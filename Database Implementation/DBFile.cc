//#include "TwoWayList.h"
//#include "Record.h"
//#include "Schema.h"
//#include "File.h"
//#include "Comparison.h"
//#include "ComparisonEngine.h"
#include "DBFile.h"
#include "Defs.h"

void *run_q (void *arg) {
	//cout<<"jklop\n"<<endl;
	bigq_util *t = (bigq_util *) arg;
	BigQ b_queue(*(t->in),*(t->out),*(t->sort_order),t->run_len);
	//cout<<"end of thread"<<endl;
}

GenericDBFile::GenericDBFile() {
}

GenericDBFile::~GenericDBFile() {
}

DBFile::DBFile() {
}

DBFile::~DBFile() {
	delete db_file;
}

int DBFile::Create (char *f_path, fType f_type, void *startup) {
	try {
		//fpath=f_path;
		//data_file.Open(0,f_path);
		file_type = f_type;
		if(f_type==heap) {
			db_file=new HeapFile();
		}
		else if (f_type==sorted) {
			db_file=new SortedFile();
		}
		else {
			cout<<"Wrong input for f_type"<<endl;
			return 0;
		}
	} catch (exception &e) {
		cout<<"Error in DBFile create"<<endl;
	}
	db_file->Create(f_path,f_type,startup);
	return 1;
}

int DBFile::Open (char *f_path) {
	try {
		//fpath=f_path;
		string s=f_path;
		s=s.substr(0,s.length()-4)+".meta";
		char* m_filename=(char*)s.c_str();
		ifstream metafile;
		metafile.open(m_filename,ios::in);
		string file_type; // variable to hold file_type
		getline(metafile,file_type);
		metafile.close();
		if(file_type.compare("heap")==0) {
			db_file=new HeapFile();
		}
		else if (file_type.compare("sorted")==0) {
			db_file=new SortedFile();
		}
		else {
			cout<<"Wrong data format in meta file"<<endl;
			return 0;
		}
	} catch (exception &e) {
		cout<<"Error in DBFile open"<<endl;
	}
	//cout<<"befor sorted open\n";
	return db_file->Open(f_path);
}

int DBFile::Close () {
	//return data_file.Close();
	return db_file->Close();
}
void DBFile::Load (Schema &f_schema, char *loadpath){
	return db_file->Load(f_schema,loadpath);
}
void DBFile::MoveFirst () {
	return db_file->MoveFirst();
}
void DBFile::Add (Record &addme) {
	return db_file->Add(addme);
}
int DBFile::GetNext (Record &fetchme){
	return db_file->GetNext(fetchme);
}
int DBFile::GetNext (Record &fetchme, CNF &cnf, Record &literal) {
	return db_file->GetNext(fetchme,cnf,literal);
}



HeapFile::HeapFile() { cur_page=0;}

HeapFile::~HeapFile() {};

int HeapFile::Open (char *f_path) {
	fpath=f_path;
	try {
		data_file.Open(1,f_path);
	} catch (exception &e) {
		cout<<"Error in heap file open"<<endl;
	}
	return 1;
}

int HeapFile::Close () {
	return data_file.Close();
}

int HeapFile::Create(char *f_path, fType f_type, void *startup) {
	fpath=f_path;
	try {
		data_file.Open(0,f_path);
		string s=f_path;
		s=s.substr(0,s.length()-4)+".meta";
		char* m_filename=(char*)s.c_str();
		ofstream metafile;
		metafile.open(m_filename,ios::trunc);
		metafile<<"heap\n";
		metafile.close();
		return 1;
	} catch (exception &e) {
		cout<<"Error in heap file create"<<endl;
	}
}

void HeapFile::Load (Schema &f_schema, char *loadpath) {
	FILE *fp=fopen(loadpath,"r");
	Record r;
	cur_page=0;
	buffer_page.EmptyItOut();
	bool rec_added=false;
	while(r.SuckNextRecord(&f_schema,fp)) {
		Add(r);
		rec_added=true;
	}
	if(rec_added) data_file.AddPage(&buffer_page,cur_page);
}

void HeapFile::MoveFirst () {
	cur_page=0;
	data_file.GetPage(&buffer_page,0);
}



void HeapFile::Add (Record &rec) {
	if(!buffer_page.Append(&rec)) {
		data_file.AddPage(&buffer_page,cur_page++);
		buffer_page.EmptyItOut();
		buffer_page.Append(&rec);
	}
}

int HeapFile::GetNext (Record &fetchme) {
	while(!buffer_page.GetFirst(&fetchme)) {
		if(cur_page<data_file.GetLength()-2) {
			data_file.GetPage(&buffer_page,++cur_page);
		}
		else	
			return 0;
	}
	return 1;
}

int HeapFile::GetNext (Record &fetchme, CNF &cnf, Record &literal) {
	ComparisonEngine comp;
	bool found = false;
	while (!found) {
		while (!buffer_page.GetFirst(&fetchme)) {
			if (cur_page < data_file.GetLength() - 2) {
				data_file.GetPage(&buffer_page, ++cur_page);
			} else
				return 0;
		}
		if (comp.Compare(&fetchme, &literal, &cnf))
			found = true;
	}
	return 1;
}

SortedFile::SortedFile() {
	search_done=0;
};

SortedFile::~SortedFile() {
};

int SortedFile::Open (char *f_path) {
	//cout<<"Open "<<f_path<<endl;
	fpath=f_path;
	try {
		data_file.Open(1,f_path);
//		cout<<"wtf1"<<endl;

		string s=f_path;
		s=s.substr(0,s.length()-4)+".meta";
		char* m_filename=(char*)s.c_str();
		ifstream metafile;
		metafile.open(m_filename,ios::in);
		string file_type; // variable to hold file_type
		string temp;

		getline(metafile,file_type);

		getline(metafile,temp);

		runlen=atoi(temp.c_str());

		getline(metafile,temp);
		sort_info=new OrderMaker();
		sort_info->numAtts=atoi(temp.c_str());

		int count=0;

//		cout<<"wtf2"<<endl;

		while(metafile.good()) {
			getline(metafile,temp);
			sort_info->whichAtts[count]=atoi(temp.c_str());
			getline(metafile,temp);
			sort_info->whichTypes[count]=(Type)atoi(temp.c_str());
			count++;
		}
		mode = reading;
/*
		in = new Pipe(PIPE_BUFFER);
		out = new Pipe(PIPE_BUFFER);
//		cout<<"wtf3"<<endl;

		bigq_util util = {in, out, sort_info, runlen};

		pthread_t thread1;

		pthread_create (&thread1, NULL,run_q, (void*)&util);
//		cout<<"wtf5"<<endl;

		//cout<<"5\n";
*/
		return 1;
	} catch (exception &e) {
		cout<<"Error in Sorted File Open"<<endl;
	}
}

void SortedFile::Add_New_File(Record &rec) {
	if(!temp_buffer_page.Append(&rec)) {
		temp_data_file.AddPage(&temp_buffer_page,temp_buffer_ptr++);
		temp_buffer_page.EmptyItOut();
		temp_buffer_page.Append(&rec);
	}
}
int SortedFile::GetNext_File(Record &fetchme) {
	while(!buffer_page.GetFirst(&fetchme)) {
		if(cur_page<data_file.GetLength()-2) {
			data_file.GetPage(&buffer_page,++cur_page);
		}
		else
			return 0;
	}
	return 1;
}

void SortedFile::Merge_with_q() {
	Record rec1;
	Record rec2;
	ComparisonEngine comp;
	temp_data_file.Open(0,"temp_dbfile.bin");
	cur_page=0;
	temp_buffer_ptr=0;
	int file_empty=0;
	int frec_status=0;
	int qrec_status=0;

	if(data_file.GetLength()>0) {
		data_file.GetPage(&buffer_page,0);
	}
	else file_empty=1;

	bool done=false;

	if(!file_empty) {
		frec_status=GetNext_File(rec1);

		qrec_status=out->Remove(&rec2);

		while(!done) {

			if(frec_status && qrec_status) {
//				if (!rec1.bits ) {
//					cout<<"caught u\n";
//				}
				if(comp.Compare(&rec1,&rec2,sort_info)<0) {
					//if(!rec1.bits) cout<<"can't add rec1 in 1st if\n";

					Add_New_File(rec1);
					frec_status=GetNext_File(rec1);
				}
				else {
				//	if(!rec2.bits) cout<<"can't add rec2 in 1st else\n";

					Add_New_File(rec2);
					qrec_status=out->Remove(&rec2);
				}
			}
			else if(frec_status) {
				do {
					//if(!rec1.bits) cout<<"can't add rec1 in 1st elsif\n";
					Add_New_File(rec1);
				} while(GetNext_File(rec1));
				done=true;
			}
			else if(qrec_status) {
				do {
					//if(!rec2.bits) cout<<"can't add rec2 in 2nd elsif\n";

					Add_New_File(rec2);
				} while(out->Remove(&rec2));
				done=true;
			}
			else {
				done=true;
			}
		}
	}
	else {
		while(out->Remove(&rec2)) {
			//if(!rec2.bits) cout<<"can't add rec2 in outer else\n";

			Add_New_File(rec2);
		}
	}

	temp_data_file.AddPage(&temp_buffer_page,temp_buffer_ptr);
	rename("temp_dbfile.bin",fpath);
	temp_data_file.Close();
}

int SortedFile::Close () {
	if(mode == writing) {
		SwitchMode();
	}
	return data_file.Close();
}

int SortedFile::Create(char *f_path, fType f_type, void *startup) {
	fpath=f_path;
	try {
		//cout<<"in sorted create\n";
		data_file.Open(0,f_path);
		//cout<<"1\n";
		//Initialize OrderMaker
		sort_info=((Sort_Info*)startup)->myOrder;
		runlen=((Sort_Info*)startup)->runLength;

		string s=f_path;
		s=s.substr(0,s.length()-4)+".meta";
		char* m_filename=(char*)s.c_str();
		ofstream metafile;
		metafile.open(m_filename,ios::trunc);
		metafile<<"sorted"<<"\n";
		metafile<<runlen<<"\n";
		metafile<<sort_info->numAtts;
		for(int i=0;i<sort_info->numAtts;i++) {
			metafile<<"\n"<<sort_info->whichAtts[i]<<"\n"<<sort_info->whichTypes[i];
		}
		mode = reading;
		/*
		cout<<"2\n";
		in = new Pipe(PIPE_BUFFER);
		out = new Pipe(PIPE_BUFFER);
		cout<<"3\n";
		bigq_util util = {in, out, sort_info, runlen};
		pthread_t thread1;
		cout<<"4\n";
		//pthread_create (&thread1, NULL,run_q, (void*)&util);
		cout<<"5\n";
		//pthread_join (thread1, NULL); */
		return 1;
	} catch (exception &e) {
		cout<<"Error in Sorted File create"<<endl;
	}
}

void SortedFile::Load (Schema &f_schema, char *loadpath) {
	if(mode == reading) {
		SwitchMode();
	}
	FILE *fp=fopen(loadpath,"r");
	Record r;
	int cnt=0;
	while(r.SuckNextRecord(&f_schema,fp)) {
		cnt++;
		Add(r);
		//cout<<cnt<<endl;
	}
}
void SortedFile::MoveFirst () {
	if(mode == writing) {
		SwitchMode();
	}
	cur_page=0;
	search_done=0;
	if(data_file.GetLength()>0)
		data_file.GetPage(&buffer_page,0);
	else
		buffer_page.EmptyItOut();
}

void SortedFile::Add (Record &rec) {
	if(mode == reading) {
		SwitchMode();
	}
	//cout<<"bef insert"<<endl;
	in->Insert(&rec);
}

int SortedFile::GetNext(Record &fetchme) {
	if(mode == writing) {
		SwitchMode();
		MoveFirst();
	}
	while(!buffer_page.GetFirst(&fetchme)) {
		if(cur_page<data_file.GetLength()-2) {
			data_file.GetPage(&buffer_page,++cur_page);
		}
		else
			return 0;
	}
	return 1;
}


int SortedFile::GetNext (Record &fetchme, CNF &cnf, Record &literal) {
	if(mode == writing) {
		SwitchMode();
		MoveFirst();
	}
	OrderMaker dummy;
	OrderMaker query_order;
	cnf.GetSortOrders(query_order,dummy);
	OrderMaker search_order;
	OrderMaker literal_order;
	ComparisonEngine comp;
	bool accept_attr = false;


	for (int i = 0; i < sort_info->numAtts; i++) {
		int att = sort_info->whichAtts[i];
		int literalIndex = cnf.HasSimpleEqualityCheck(att);
		if (literalIndex != -1) {
			search_order.whichAtts[i]=att;
			search_order.whichTypes[i]=sort_info->whichTypes[i];
			literal_order.whichAtts[i]=literalIndex;
			literal_order.whichTypes[i]=sort_info->whichTypes[i];
			search_order.numAtts++;
			literal_order.numAtts++;
		}
		else {
			break;
		}
	}

	//search_order.Print();
	//literal_order.Print();
	if(search_order.numAtts==0) {
		cout<<"no bs\n";
		return GetNext_NO_BS(fetchme, cnf, literal);
	}
	if(search_done) {
		cout<<"in search done\n";
		return GetNext_With_BS(fetchme,cnf,literal,search_order,literal_order);
	}
	cout<<"gng for 1st match\n";
	int f_page;
	int l_page;
	int m_page;
	int file_length;

	if(data_file.GetLength()) {
		file_length=data_file.GetLength()-1;
	}
	else {
		return 0;
	}

	f_page=0;
	l_page=file_length-1;

	if(file_length==1) {
		return GetNext_NO_BS(fetchme, cnf, literal);
	}
//	cout<<"hmm1"<<endl;
//	literal.Print(&Schema("catalog","lineitem"));
	//cout<<"hmm"<<endl;
	while(f_page<(l_page-1)) {
		m_page=(f_page+l_page)/2;
		//cout<<"mpage: "<<m_page<<endl;
		data_file.GetPage(&buffer_page,m_page);
		buffer_page.GetFirst(&fetchme);
		//fetchme.Print(&Schema("catalog","lineitem"));
		int comparison=comp.Compare (&fetchme,&search_order,&literal, &literal_order);
		//cout<<"comp: "<<comparison<<endl;
		if (comparison>=0) {
			l_page=m_page;
		}
		else if(comparison<0) {
			f_page=m_page;
		}
		else {
			cout<<"Wrong comparison"<<endl;
		}
	}

	search_done=1;

	bool match_found=false;
	//cout<<"fpage: "<<f_page<<" lpage: "<<l_page<<endl;

	data_file.GetPage(&buffer_page,f_page);

	if(GetFirst_Match(fetchme, literal, cnf, search_order,literal_order)) {
		cur_page=f_page;
		match_found=true;
	}
	else {
		//cur_page=f_page;
		data_file.GetPage(&buffer_page,f_page+1);
		if(GetFirst_Match(fetchme, literal, cnf, search_order,literal_order)) {
			cur_page=f_page+1;
			match_found=true;
		}
	}
	if(!match_found) return 0;

	return comp.Compare(&fetchme,&literal,&cnf) || GetNext_With_BS(fetchme,cnf,literal,search_order,literal_order);
}

int SortedFile::GetFirst_Match(Record &fetchme, Record &literal, CNF &cnf, OrderMaker &search_order,OrderMaker &literal_order) {
	ComparisonEngine comp;
	//cout<<"getnext first match"<<endl;

	while(buffer_page.GetFirst(&fetchme)) {
		//fetchme.Print(&Schema("catalog","lineitem"));
		if (!comp.Compare (&fetchme,&search_order,&literal, &literal_order)) {
				return 1;
		}
		else {
			continue;
		}
	}
	//return 0;
	//cout<<"ghyuji"<<endl;
	return 0;
}

int SortedFile::GetNext_NO_BS(Record &fetchme, CNF &cnf, Record &literal) {
	//cout<<"getnext no bs"<<endl;
	ComparisonEngine comp;
		bool found = false;
		while (!found) {
			while (!buffer_page.GetFirst(&fetchme)) {
				if (cur_page < data_file.GetLength() - 2) {
					data_file.GetPage(&buffer_page, ++cur_page);
				} else
					return 0;
			}
			if (comp.Compare(&fetchme, &literal, &cnf))
				found = true;
		}
		return 1;
}

int SortedFile::GetNext_With_BS(Record &fetchme, CNF &cnf, Record &literal,
		OrderMaker &search_order,OrderMaker &literal_order) {
	//cout<<"in getnext with bs\n";
	bool found = false;
	ComparisonEngine comp;

		while (!found) {
			while (!buffer_page.GetFirst(&fetchme)) {
				if (cur_page < data_file.GetLength() - 2) {
					data_file.GetPage(&buffer_page, ++cur_page);
				} else
					return 0;
			}
			if (!comp.Compare (&fetchme,&search_order,&literal, &literal_order)) {
				if(comp.Compare(&fetchme,&literal,&cnf))
					found = true;
				else continue;
			}
			else
				return 0;
		}
		return 1;
}


void SortedFile::SwitchMode() {
	if(mode == reading) {
		//cout<<"in reading"<<endl;
		mode = writing;
		in = new  (std::nothrow) Pipe(PIPE_BUFFER);
		out = new (std::nothrow) Pipe(PIPE_BUFFER);
		util = new bigq_util();
		util->in=in;
		util->out=out;
		util->sort_order=sort_info;
		util->run_len=runlen;
		pthread_create (&thread1, NULL,run_q, (void*)util);
	}
	else if(mode == writing)  {
		//cout<<"in writing"<<endl;
		mode = reading;
		in->ShutDown();
		Merge_with_q();
		delete util;
		delete in;
		delete out;
		in=NULL;
		out=NULL;
	}
}
