#ifndef DBFILE_H
#define DBFILE_H

#include <stdio.h>
#include <iostream>
#include <pthread.h>
#include "TwoWayList.h"
#include "Record.h"
#include "Schema.h"
#include "File.h"
#include "Comparison.h"
#include "ComparisonEngine.h"
#include "Pipe.h"
#include "BigQ.h"
#include <exception>
#include <fstream>
#include <string>

#define PIPE_BUFFER 100

using namespace std;
typedef enum {heap, sorted, tree} fType;
typedef enum {reading, writing} file_mode;

typedef struct SortInfo {
 OrderMaker *myOrder;
 int runLength;
} Sort_Info;



//typedef struct {
//	Pipe *in;
//	Pipe *out;
//	OrderMaker* sort_order;
//	int run_len;
//} bigq_util;

class GenericDBFile {

public:
	GenericDBFile();
	virtual ~GenericDBFile ()=0;
	virtual int Create (char *fpath, fType file_type, void *startup)=0;
	virtual int Open (char *fpath)=0;
	virtual int Close ()=0;

	virtual void Load (Schema &f_schema, char *loadpath)=0;

	virtual void MoveFirst ()=0;
	virtual void Add (Record &addme)=0;
	virtual int GetNext (Record &fetchme)=0;
	virtual int GetNext (Record &fetchme, CNF &cnf, Record &literal)=0;

};

class HeapFile : public GenericDBFile {

private:
	char* fpath;
	Page buffer_page;
	off_t cur_page;
	File data_file;
	ofstream metadata_file;

public:
	HeapFile ();
	~HeapFile ();
	int Create (char *fpath, fType file_type, void *startup);
	int Open (char *fpath);
	int Close ();
	void Load (Schema &f_schema, char *loadpath);
	void MoveFirst ();
	void Add (Record &addme);
	int GetNext (Record &fetchme);
	int GetNext (Record &fetchme, CNF &cnf, Record &literal);
};

class SortedFile : public GenericDBFile {

private:
	char* fpath;
	File data_file;
	File temp_data_file;
	ofstream metadata_file;
	Page buffer_page;
	Page temp_buffer_page;
	off_t cur_page;
	off_t temp_buffer_ptr;
	OrderMaker* sort_info;
	Pipe* in;
	Pipe* out;
	file_mode mode;
	//BigQ b_queue;
	int runlen;
	//int count=0;
	int search_done;
	bigq_util* util;
	pthread_t thread1;

public:
	SortedFile ();
	~SortedFile ();
	int Create (char *fpath, fType file_type, void *startup);
	int Open (char *fpath);
	int Close ();
	void Load (Schema &f_schema, char *loadpath);
	void MoveFirst ();
	void Add (Record &addme);
	int GetNext (Record &fetchme);
	int GetNext (Record &fetchme, CNF &cnf, Record &literal);
	void SwitchMode();
	//void RunBigQ(bigq_util*);
	void Merge_with_q();
	void Add_New_File(Record &addme);
	int GetNext_File(Record &fetchme);
	int GetFirst_Match(Record &fetchme, Record &literal, CNF &cnf, OrderMaker &search_order,
			OrderMaker &literal_order);
	int GetNext_NO_BS(Record &fetchme, CNF &cnf, Record &literal);
	int GetNext_With_BS(Record &fetchme, CNF &cnf, Record &literal,OrderMaker &order,OrderMaker &literal_order);
};

class DBFile {

private:
	fType file_type;
	GenericDBFile* db_file;

public:
	char *fpath;
	DBFile ();
	~DBFile ();
	int Create (char *fpath, fType file_type, void *startup);
	int Open (char *fpath);
	int Close ();

	void Load (Schema &f_schema, char *loadpath);
	void MoveFirst ();
	void Add (Record &addme);
	int GetNext (Record &fetchme);
	int GetNext (Record &fetchme, CNF &cnf, Record &literal);
};
#endif


