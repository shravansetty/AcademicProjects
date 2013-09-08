#ifndef STATISTICS_H
#define STATISTICS_H

#include<iostream>
#include<fstream>
#include<string>
#include<vector>
#include<algorithm>
#include<map>
#include<set>
#include "ParseTree.h"
using namespace std;

struct RelationInfo {
	unsigned long long int totalNumbOfRows;
	int numbOfPartition;
	map <string, unsigned long long int> Attributes;
    
	RelationInfo() : totalNumbOfRows(0), numbOfPartition(-1)
	{}		
};


class Statistics
{
private:
	int partitionNumber;
	bool isCopiedStats;
	
	map <int, vector<string> > PartInfo;
	map <string, vector <string> > colToRelation;
	map <string, RelationInfo> Statistics1;
	map <string, RelationInfo> Statistics2;
        
    struct ColumnCountAndEstimate
    {
        int repeatCount;
        long double estimate;
    };

    bool validateParseTree(struct AndList *parseTree, char *relNames[], int numToJoin, vector<string>&, set<string>&);
 	
public:
	Statistics();
	~Statistics();
	Statistics(Statistics &copyMe);

	void AddRel(char *relName, int numTuples);
	void CopyRel(char *oldName, char *newName);
	void AddAtt(char *relName, char *attName, int numDistincts);
	
	void  Apply(struct AndList *parseTree, char *relNames[], int numToJoin);
	double Estimate(struct AndList *parseTree, char **relNames, int numToJoin);
	
	void Read(char *fromWhere);
	void Write(char *fromWhere);

	int GetNumberOfPartition();
	map<int, vector<string> > * GetPartsInfo();
	map<string, RelationInfo> * GetStatistics();
};

#endif
