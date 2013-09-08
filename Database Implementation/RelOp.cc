#include "RelOp.h"
#include <vector>
#include <iterator>

void *run_bigq(void *arg) {
	bigq_util *t = (bigq_util *) arg;
	BigQ b_queue(*(t->in), *(t->out), *(t->sort_order), t->run_len);
}

typedef struct {
	CNF* selOp;
	Record* literal;
	Pipe* inPipe;
	Pipe* outPipe;
} select_Pipe;

typedef struct {
	CNF* selOp;
	Record* literal;
	DBFile* inFile;
	Pipe* outPipe;
} select_File;

typedef struct {
	Function* computeMe;
	Pipe* inPipe;
	Pipe* outPipe;
} sumUtil;

typedef struct {
	CNF* selOp;
	Record* literal;
	int mem_pages;
	Pipe* inPipeL;
	Pipe* inPipeR;
	Pipe* outPipe;

} joinUtil;

typedef struct {
	int* keepMe;
	int numAttsInput;
	int numAttsOutput;
	Pipe* inPipe;
	Pipe* outPipe;
} projectUtil;

typedef struct {
	Schema* mySchema;
	int runlen;
	Pipe* inPipe;
	Pipe* outPipe;

} duplicateRemoveUtil;

typedef struct {
	OrderMaker* groupAtts;
	Function* computeMe;
	int mem_pages;
	Pipe* inPipe;
	Pipe* outPipe;
} groupByUtil;


void *selectFile_worker(void *arg) {
	select_File* selectFileData = (select_File*) arg;
	Record tmpRecord;
	ComparisonEngine compEngine;
	selectFileData->inFile->MoveFirst();
	while (selectFileData->inFile->GetNext(tmpRecord)) {
		if (compEngine.Compare(&tmpRecord, selectFileData->literal, selectFileData->selOp))
			selectFileData->outPipe->Insert(&tmpRecord);
	}
	selectFileData->outPipe->ShutDown();
}

void *selectPipe_worker(void *arg) {
	select_Pipe* selectPipeData = (select_Pipe*) arg;
	Record tempRecord;
	ComparisonEngine compEngine;
	while (selectPipeData->inPipe->Remove(&tempRecord)) {
		if (compEngine.Compare(&tempRecord, selectPipeData->literal, selectPipeData->selOp))
			selectPipeData->outPipe->Insert(&tempRecord);
	}
	selectPipeData->outPipe->ShutDown();
}

void *project_worker(void *arg) {
	projectUtil* projectData = (projectUtil*) arg;
	Record tempRecord;
	while (projectData->inPipe->Remove(&tempRecord)) {
		tempRecord.Project(projectData->keepMe, projectData->numAttsOutput,
				projectData->numAttsInput);
		projectData->outPipe->Insert(&tempRecord);
	}
	projectData->outPipe->ShutDown();
}

void *sum_worker(void *arg) {
	sumUtil* sumData = (sumUtil*) arg;
	Record tempRecord;
	Type ret_type;
	int sum_Integer = 0;
	double sum_Double = 0.0;
	while (sumData->inPipe->Remove(&tempRecord)) {
		int int_res = 0;
		double double_res = 0.0;
		ret_type = sumData->computeMe->Apply(tempRecord, int_res, double_res);
		if (ret_type == Int) {
			sum_Integer += int_res;
		} else {
			sum_Double += double_res;
		}
	}
	char src[20];
	Attribute atts;

	if (ret_type == Int) {
		atts.myType = Int;
		sprintf(src, "%d|", sum_Integer);
	} else {
		atts.myType = Double;
		sprintf(src, "%.2f|", sum_Double);
	}
	atts.name = "count";
	Schema countSchema("out_count", 1, &atts);

	Record sumRec;
	sumRec.ComposeRecord(&countSchema, src);
	sumData->outPipe->Insert(&sumRec);
	sumData->outPipe->ShutDown();

}
void* join_worker(void* arg) {
	joinUtil* joinData = (joinUtil*) arg;

	Pipe *inPipeLeft = joinData->inPipeL;
	Pipe *inPipeRight = joinData->inPipeR;
	Pipe *outPipe = joinData->outPipe;
	CNF *selOp = joinData->selOp;
	Record *literal = joinData->literal;
	int memoryPages = joinData->mem_pages;

	OrderMaker left, right;
	int canUseSortMerge;
	canUseSortMerge = selOp->GetSortOrders(left, right);
	ComparisonEngine compEngine;
	int join_calc_done = 0, numAttsLeft, numAttsRight, numAttsToKeep,
			startOfRight;
	int* attsToKeep;
	if (canUseSortMerge) {
		Record record1, record2;
		Pipe* PipeLeft;
		Pipe* PipeRight;
		PipeLeft = new (std::nothrow) Pipe(PIPE_BUFFER);
		PipeRight = new (std::nothrow) Pipe(PIPE_BUFFER);
		bigq_util* q1util = new (std::nothrow) bigq_util();
		q1util->in = inPipeLeft;
		q1util->out = PipeLeft;
		q1util->run_len = memoryPages;
		q1util->sort_order = &left;

		bigq_util* q2util = new (std::nothrow) bigq_util();
		q2util->in = inPipeRight;
		q2util->out = PipeRight;
		q2util->run_len = memoryPages;
		q2util->sort_order = &right;

		pthread_t thread1, thread2;
		pthread_create(&thread1, NULL, run_bigq, (void*) q1util);

		pthread_create(&thread2, NULL, run_bigq, (void*) q2util);

		int q1Status = PipeLeft->Remove(&record1);
		int q2Status = PipeRight->Remove(&record2);
		int count = 0;

		Record mergedRecord;

		while (true) {
			if (q1Status && q2Status) {

				if (compEngine.Compare(&record1, &left, &record2, &right) < 0) {
					q1Status = PipeLeft->Remove(&record1);
					continue;

				} else if (compEngine.Compare(&record1, &left, &record2, &right) > 0) {
					q2Status = PipeRight->Remove(&record2);
					continue;
				} else {
					Record* temp = new (std::nothrow) Record();
					temp->Consume(&record2);
					vector<Record*> recordVector;

					while (q2Status
							&& !compEngine.Compare(&record1, &left, temp, &right)) {
						recordVector.push_back(temp);
						temp = new (std::nothrow) Record();
						q2Status = PipeRight->Remove(temp);
					}

					if (q2Status)
						record2.Consume(temp);

					delete temp;
					temp = NULL;
					while (q1Status
							&& !compEngine.Compare(&record1, &left, recordVector.front(),
									&right)) {

						for (int i = 0; i < recordVector.size(); i++) {
							if (compEngine.Compare(&record1, recordVector[i], literal,
									selOp)) {
								if (!join_calc_done) {
									numAttsLeft = ((((int*) (record1.bits))[1])
											/ sizeof(int)) - 1;
									numAttsRight =
											((((int*) (recordVector[i]->bits))[1])
													/ sizeof(int)) - 1;
									numAttsToKeep = numAttsLeft + numAttsRight;

									attsToKeep =
											new (std::nothrow) int[numAttsToKeep];
									int k;
									for (k = 0; k < numAttsLeft; k++) {
										attsToKeep[k] = k;
									}

									startOfRight = k;

									for (int l = 0; l < numAttsRight;
											l++, k++) {
										attsToKeep[k] = l;
									}
									join_calc_done = 1;
								}

								mergedRecord.MergeRecords(&record1, recordVector[i],
										numAttsLeft, numAttsRight, attsToKeep,
										numAttsToKeep, startOfRight);
								outPipe->Insert(&mergedRecord);

							}
						}
						q1Status = PipeLeft->Remove(&record1);

					}

					for (int i = 0; i < recordVector.size(); i++) {
						delete recordVector[i];
					}

					recordVector.empty();
				}
			}

			else {
				break;
			}
		}
		delete PipeLeft;
		delete PipeRight;
		delete attsToKeep;
	} else {

		Page page;
		int count = 0;
		vector<Record*> record1vector;
		vector<Record*> record2vector;
		int record1Status = 0;
		int file_ready = 0;
		Record record1, record2;
		Record* tempRecord;
		File f;
		f.Open(0, "temp.bin");
		int currentPage = 0;
		int scanPtr = 0;
		int pipe2Status = 0;
		int cnt = 0, cnt2 = 0;
		while (true) {
			if ((record1Status = inPipeLeft->Remove(&record1))
					&& count < (memoryPages - 1)) {

				if (!page.Append(&record1)) {

					tempRecord = new (std::nothrow) Record();
					while (page.GetFirst(tempRecord)) {

						record1vector.push_back(tempRecord);
						tempRecord = new Record();
					}

					delete tempRecord;
					tempRecord = NULL;
					page.Append(&record1);
					count++;
				}

			} else {
				cnt += record1vector.size();

				int scan_done = 0, scan_ptr = 0;
				Page scan_page;

				if (count < memoryPages - 1) {
					tempRecord = new (std::nothrow) Record();
					while (page.GetFirst(tempRecord)) {
						record1vector.push_back(tempRecord);
						tempRecord = new (std::nothrow) Record();
					}
					delete tempRecord;
					tempRecord = NULL;
				}

				while (true) {
					if (!file_ready) {

						while ((pipe2Status = inPipeRight->Remove(&record2))
								&& scan_page.Append(&record2)) {
						}

						Page temp_page;
						Record* temp1;
						tempRecord = new (std::nothrow) Record();
						temp1 = new (std::nothrow) Record();
						while (scan_page.GetFirst(tempRecord)) {
							temp1->Copy(tempRecord);
							record2vector.push_back(tempRecord);
							temp_page.Append(temp1);
							tempRecord = new (std::nothrow) Record();
							temp1 = new (std::nothrow) Record();
						}
						delete tempRecord;
						delete temp1;
						tempRecord = NULL;
						temp1 = NULL;

						f.AddPage(&temp_page, currentPage++);
						temp_page.EmptyItOut();
						scan_page.EmptyItOut(); //

					} else {
						if (scan_ptr < f.GetLength() - 1) {
							f.GetPage(&scan_page, scan_ptr++);
							tempRecord = new (std::nothrow) Record();
							while (scan_page.GetFirst(tempRecord)) {
								record2vector.push_back(tempRecord);
								tempRecord = new (std::nothrow) Record();

							}
							delete tempRecord;
							tempRecord = NULL;
							scan_page.EmptyItOut();
						} else
							scan_done = 1;
					}

					cnt += record2vector.size();

					for (int i = 0; i < record1vector.size(); i++) {
						for (int j = 0; j < record2vector.size(); j++) {

							if (compEngine.Compare(record1vector[i], record2vector[j],
									literal, selOp)) {
								Record mergedRecord;
								if (!join_calc_done) {
									numAttsLeft =
											((((int*) (record1vector[i]->bits))[1])
													/ sizeof(int)) - 1;
									numAttsRight =
											((((int*) (record2vector[j]->bits))[1])
													/ sizeof(int)) - 1;
									numAttsToKeep = numAttsLeft + numAttsRight;

									attsToKeep =
											new (std::nothrow) int[numAttsToKeep];
									int k;
									for (k = 0; k < numAttsLeft; k++) {
										attsToKeep[k] = k;
									}

									startOfRight = k;

									for (int l = 0; l < numAttsRight;
											l++, k++) {
										attsToKeep[k] = l;
									}
									join_calc_done = 1;
								}

								mergedRecord.MergeRecords(record1vector[i],
										record2vector[j], numAttsLeft,
										numAttsRight, attsToKeep, numAttsToKeep,
										startOfRight);
								outPipe->Insert(&mergedRecord);

							}
						}

					}

					for (int i = 0; i < record2vector.size(); i++) {
						delete record2vector[i];
						record2vector[i] = NULL;
					}

					record2vector.clear();

					if (!pipe2Status && !file_ready) {
						file_ready = 1;
						break;
					} else if (!file_ready) {
						scan_page.Append(&record2);
					}

					if (scan_done) {
						break;
					}

				}

				for (int i = 0; i < record1vector.size(); i++) {
					delete record1vector[i];
					record1vector[i] = NULL;
				}


				record1vector.clear();

				if (count >= (memoryPages - 1)) {
					page.Append(&record1);
					cnt++;
					count = 0;

				} else
					break;


			}
		}

		f.Close();
		remove("temp.bin");
	}

	outPipe->ShutDown();

}

void *groupBy_worker(void *arg) {
	groupByUtil* groupByData = (groupByUtil*) arg;
	Pipe* inPipe = groupByData->inPipe;
	Pipe* outPipe = groupByData->outPipe;
	OrderMaker* groupAtts = groupByData->groupAtts;
	Function* computeMe = groupByData->computeMe;
	Record groupSample, curRec;

	Pipe* sortedPipe;
	sortedPipe = new (std::nothrow) Pipe(PIPE_BUFFER);

	bigq_util* util1 = new bigq_util();
	util1->in = inPipe;
	util1->out = sortedPipe;
	util1->run_len = groupByData->mem_pages;
	util1->sort_order = groupAtts;
	int cnt=0;

	pthread_t thread1;
	pthread_create(&thread1, NULL, run_bigq, (void*) util1);

	sortedPipe->Remove(&groupSample);

	ComparisonEngine compEng;
	Type retType;
	int int_res = 0, int_sum = 0;
	double double_res = 0.0, double_sum = 0.0;
	Attribute sumAtt;
	sumAtt.name = "group_sum";

	retType = computeMe->Apply(groupSample, int_res, double_res);

	if (retType == Int) {
		sumAtt.myType = Int;
		int_sum += int_res;
	} else {
		sumAtt.myType = Double;
		double_sum += double_res;
	}
//create a new schema
	Schema countSchema("out_count", 1, &sumAtt);
	Record finalRec;
	char src[20];
	int groupNum = 1;

	while (sortedPipe->Remove(&curRec)) {
		if (compEng.Compare(&groupSample, &curRec, groupAtts)) {

			if (retType == Int) {
				sprintf(src, "%d|", int_sum);
			} else {
				sprintf(src, "%.2f|", double_sum);
			}
			++groupNum;

			finalRec.ComposeRecord(&countSchema, src);
			outPipe->Insert(&finalRec);

			groupSample.Consume(&curRec);
			int_sum = 0;
			double_sum = 0.0;
			retType = computeMe->Apply(groupSample, int_res, double_res);
			if (retType == Int) {
				int_sum += int_res;
			} else {
				double_sum += double_res;
			}
		} else {
			retType = computeMe->Apply(curRec, int_res, double_res);
			if (retType == Int) {
				int_sum += int_res;
			} else {
				double_sum += double_res;
			}
		}
	}

	if (retType == Int) {
		sprintf(src, "%d|", int_sum);
	} else {
		sprintf(src, "%.2f|", double_sum);
	}

	finalRec.ComposeRecord(&countSchema, src);
	outPipe->Insert(&finalRec);
	outPipe->ShutDown();

}


void SelectFile::Run(DBFile &inFile, Pipe &outPipe, CNF &selOp,
		Record &literal) {
	select_File* sf_data = new (std::nothrow) select_File();
	sf_data->inFile = &inFile;
	sf_data->outPipe = &outPipe;
	sf_data->selOp = &selOp;
	sf_data->literal = &literal;

	pthread_create(&thread1, NULL, selectFile_worker, (void*) sf_data);
}

void SelectFile::WaitUntilDone() {
	pthread_join(thread1, NULL);
}

void SelectFile::Use_n_Pages(int n) {
	run_length = n;
}



void SelectPipe::Run(Pipe &inPipe, Pipe &outPipe, CNF &selOp, Record &literal) {
	select_Pipe* sp_data = new (std::nothrow) select_Pipe();
	sp_data->inPipe = &inPipe;
	sp_data->outPipe = &outPipe;
	sp_data->selOp = &selOp;
	sp_data->literal = &literal;

	pthread_create(&thread1, NULL, selectPipe_worker, (void*) sp_data);

}

void SelectPipe::WaitUntilDone() {
	pthread_join(thread1, NULL);
}

void SelectPipe::Use_n_Pages(int n) {
	run_length = n;
}

void Join::Run(Pipe &inPipeL, Pipe &inPipeR, Pipe &outPipe, CNF &selOp,
		Record &literal) {
	joinUtil* j_data = new (std::nothrow) joinUtil();
	j_data->inPipeL = &inPipeL;
	j_data->inPipeR = &inPipeR;
	j_data->outPipe = &outPipe;
	j_data->selOp = &selOp;
	j_data->literal = &literal;
	j_data->mem_pages = mem_pages;
	pthread_create(&thread1, NULL, join_worker, (void*) j_data);

}

void Join::WaitUntilDone() {
	pthread_join(thread1, NULL);
}

void Join::Use_n_Pages(int n) {
	mem_pages = n;
}

void Project::Run(Pipe &inPipe, Pipe &outPipe, int *keepMe, int numAttsInput,
		int numAttsOutput) {
	projectUtil* p_data = new (std::nothrow) projectUtil();
	p_data->inPipe = &inPipe;
	p_data->outPipe = &outPipe;
	p_data->keepMe = keepMe;
	p_data->numAttsInput = numAttsInput;
	p_data->numAttsOutput = numAttsOutput;

	pthread_create(&thread1, NULL, project_worker, (void*) p_data);

}

void Project::WaitUntilDone() {
	pthread_join(thread1, NULL);
}

void Project::Use_n_Pages(int n) {
}

void Sum::Run(Pipe &inPipe, Pipe &outPipe, Function &computeMe) {
	sumUtil* t_data = new (std::nothrow) sumUtil();
	t_data->inPipe = &inPipe;
	t_data->outPipe = &outPipe;
	t_data->computeMe = &computeMe;
	pthread_create(&thread1, NULL, sum_worker, (void*) t_data);

}

void Sum::WaitUntilDone() {
	pthread_join(thread1, NULL);
}

void Sum::Use_n_Pages(int n) {

}


void GroupBy::Run(Pipe &inPipe, Pipe &outPipe, OrderMaker &groupAtts,
		Function &computeMe) {
	groupByUtil* gb_data = new (std::nothrow) groupByUtil();
	gb_data->inPipe = &inPipe;
	gb_data->outPipe = &outPipe;
	gb_data->computeMe = &computeMe;
	gb_data->groupAtts = &groupAtts;
	gb_data->mem_pages=mem_pages;
	pthread_create(&thread1, NULL, groupBy_worker, (void*) gb_data);
}

void GroupBy::WaitUntilDone() {
	pthread_join(thread1, NULL);
}

void GroupBy::Use_n_Pages(int n) {
	mem_pages=n;
}


void *dr_run(void *arg) {
	duplicateRemoveUtil* dr_data = (duplicateRemoveUtil*) arg;
	Pipe* inPipe = dr_data->inPipe;
	Pipe* outPipe = dr_data->outPipe;
	Schema* mySchema = dr_data->mySchema;

	OrderMaker allAtts(mySchema);
	Record prevRec, curRec;

	Pipe* sortedPipe;
	sortedPipe = new Pipe(PIPE_BUFFER);

	bigq_util* util1 = new bigq_util();
	util1->in = inPipe;
	util1->out = sortedPipe;
	util1->run_len = dr_data->runlen;
	util1->sort_order = &allAtts;

	pthread_t thread1;
	pthread_create(&thread1, NULL, run_bigq, (void*) util1);

	ComparisonEngine compEng;
	sortedPipe->Remove(&prevRec);
	int cnt=0;
	while (sortedPipe->Remove(&curRec)) {
		if (compEng.Compare(&prevRec, &curRec, &allAtts)) {
			outPipe->Insert(&prevRec);
			prevRec.Consume(&curRec);
		}
	}
	outPipe->Insert(&prevRec);

	outPipe->ShutDown();

}

void DuplicateRemoval::Run (Pipe &inPipe, Pipe &outPipe, Schema &mySchema) {
	duplicateRemoveUtil* dr_data = new (std::nothrow) duplicateRemoveUtil();
	dr_data->inPipe = &inPipe;
	dr_data->outPipe = &outPipe;
	dr_data->mySchema = &mySchema;
	dr_data->runlen=mem_pages;
	pthread_create(&thread1, NULL, dr_run, (void*) dr_data);
}

void DuplicateRemoval::WaitUntilDone () {
	pthread_join(thread1, NULL);
}

void DuplicateRemoval::Use_n_Pages (int n) {
	mem_pages=n;
}

typedef struct {
	Pipe* inPipe;
	FILE* outFile;
	Schema* mySchema;
} wo_util;


void *wo_run(void *arg) {
	wo_util* wo_data = (wo_util*) arg;
	Pipe* inPipe = wo_data->inPipe;
	FILE* outFile = wo_data->outFile;
	Schema* mySchema = wo_data->mySchema;
	int numAtts = mySchema->numAtts;
	Attribute *attsArr = mySchema->myAtts;
	Record tmp;
	int cnt=0;

	while (inPipe->Remove(&tmp)) {
		for (int i = 0; i < numAtts; i++) {
			fputs(attsArr[i].name, outFile);
			fputs(": ", outFile);
			int startPtr = ((int *) tmp.bits)[i + 1];
			fputs("[", outFile);
			char s[200];
			if (attsArr[i].myType == Int) {

				int *val = (int *)(tmp.bits+startPtr);
				sprintf(s, "%d", *val);
				fputs(s, outFile);
			} else if (attsArr[i].myType == Double) {
				double *val = (double *) (tmp.bits+startPtr);
				sprintf(s, "%f", *val);
				fputs(s, outFile);
			} else if (attsArr[i].myType == String) {

				char *val = (char *) (tmp.bits+startPtr);
				fputs(val, outFile);
			}

			fputs("]", outFile);


			if (i != numAtts - 1) {
				fputs(", ", outFile);
			}

		}
		fputs("\n", outFile);
	}
	fclose (outFile);

}


void WriteOut::Run (Pipe &inPipe, FILE *outFile, Schema &mySchema) {
	wo_util* wo_data = new (std::nothrow) wo_util();
	wo_data->inPipe = &inPipe;
	wo_data->outFile = outFile;
	wo_data->mySchema = &mySchema;
	pthread_create(&thread1, NULL, wo_run, (void*) wo_data);
}

void WriteOut::WaitUntilDone () {
	pthread_join(thread1, NULL);
}
void WriteOut::Use_n_Pages (int n) { }

