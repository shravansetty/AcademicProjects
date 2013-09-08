#ifndef REL_OP_H
#define REL_OP_H

#include "Pipe.h"
#include "DBFile.h"
#include "Record.h"
#include "Function.h"

//typedef struct {
//	Pipe *in;
//	Pipe *out;
//	OrderMaker* sort_order;
//	int run_len;
//} bigq_util;

class RelationalOp {

	public:
	// blocks the caller until the particular relational operator 
	// has run to completion
	virtual void WaitUntilDone () = 0;

	// tell us how much internal memory the operation can use
	virtual void Use_n_Pages (int n) = 0;
};

class SelectFile : public RelationalOp { 

	private:
	int run_length;
	pthread_t thread1;
	public:
	SelectFile() { run_length=10;}
	void Run (DBFile &inFile, Pipe &outPipe, CNF &selOp, Record &literal);
	void WaitUntilDone ();
	void Use_n_Pages (int n);

};

class SelectPipe : public RelationalOp {
	private:
	pthread_t thread1;
	int run_length;
	public:
	SelectPipe() {run_length=10;}
	void Run (Pipe &inPipe, Pipe &outPipe, CNF &selOp, Record &literal);
	void WaitUntilDone ();
	void Use_n_Pages (int n);
};

class Project : public RelationalOp { 
	private:
	pthread_t thread1;

	public:
	Project() {};
	void Run (Pipe &inPipe, Pipe &outPipe, int *keepMe, int numAttsInput, int numAttsOutput);
	void WaitUntilDone ();
	void Use_n_Pages (int n);
};
class Join : public RelationalOp { 
	private:
	int mem_pages;
	pthread_t thread1;
	public:
	Join() { mem_pages=10;}
	void Run (Pipe &inPipeL, Pipe &inPipeR, Pipe &outPipe, CNF &selOp, Record &literal);
	void WaitUntilDone ();
	void Use_n_Pages (int n);
};
class DuplicateRemoval : public RelationalOp {
	pthread_t thread1;
	int mem_pages;
	public:
	DuplicateRemoval() {mem_pages=10;}
	void Run (Pipe &inPipe, Pipe &outPipe, Schema &mySchema);
	void WaitUntilDone ();
	void Use_n_Pages (int n);
};
class Sum : public RelationalOp {
	private:
	pthread_t thread1;
	public:
	Sum() {}
	void Run (Pipe &inPipe, Pipe &outPipe, Function &computeMe);
	void WaitUntilDone ();
	void Use_n_Pages (int n);
};
class GroupBy : public RelationalOp {
	private:
	pthread_t thread1;
	int mem_pages;
	public:
	GroupBy() {mem_pages=10;}
	void Run (Pipe &inPipe, Pipe &outPipe, OrderMaker &groupAtts, Function &computeMe);
	void WaitUntilDone ();
	void Use_n_Pages (int n);
};
class WriteOut : public RelationalOp {
	pthread_t thread1;
	public:
	WriteOut() {}
	void Run (Pipe &inPipe, FILE *outFile, Schema &mySchema);
	void WaitUntilDone ();
	void Use_n_Pages (int n);
};
#endif
