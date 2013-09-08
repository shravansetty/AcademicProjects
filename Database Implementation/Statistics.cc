#include <string.h>
#include <sstream>
#include "Statistics.h"
using namespace std;

Statistics::Statistics() : partitionNumber(0), isCopiedStats(false)
{}

Statistics::Statistics(Statistics &copyMe)
{
	partitionNumber = copyMe.GetNumberOfPartition();
	
	map <string, RelationInfo> * copyOfStatistics = copyMe.GetStatistics();

	for (map <string, RelationInfo>::iterator statsIterator = copyOfStatistics->begin(); statsIterator != copyOfStatistics->end(); statsIterator++) {
		RelationInfo relationInfo;
		relationInfo.totalNumbOfRows = statsIterator->second.totalNumbOfRows;
		relationInfo.numbOfPartition = statsIterator->second.numbOfPartition;
		
		for (map <string, unsigned long long int >::iterator attributesIterator = statsIterator->second.Attributes.begin(); attributesIterator != statsIterator->second.Attributes.end(); attributesIterator++) {
			relationInfo.Attributes[attributesIterator->first] = attributesIterator->second;
		}
		
		Statistics1[statsIterator->first] = relationInfo;
	}

	map <int, vector<string> > * copyPartitionsInfo = copyMe.GetPartsInfo();

	for (map <int, vector<string> >::iterator partitionInfoIterator = copyPartitionsInfo->begin(); partitionInfoIterator != copyPartitionsInfo->end(); partitionInfoIterator++) {
		vector<string> relationNames;
		
		vector<string> * copyRelationNames = &partitionInfoIterator->second;
		for (int i = 0; i < copyRelationNames->size(); i++) {
			relationNames.push_back(copyRelationNames->at(i));
		}
		
		PartInfo[partitionInfoIterator->first] = relationNames;
	}
}

Statistics::~Statistics()
{ }

void Statistics::AddRel(char *relName, int totalNumRows)
{
	map <string, RelationInfo>::iterator statsIterator = Statistics1.find(string(relName));

	if (statsIterator == Statistics1.end()) {
		RelationInfo relationInfo;
		relationInfo.totalNumbOfRows = totalNumRows;
		Statistics1[string(relName)] = relationInfo;
	}
	else {
		statsIterator->second.totalNumbOfRows = totalNumRows;
	}
}

void Statistics::AddAtt(char *relName, char *attName, int numDistincts)
{
    map <string, RelationInfo>::iterator statsIterator = Statistics1.find(string(relName));

    if (statsIterator == Statistics1.end()) {
        RelationInfo rel_info;
		rel_info.Attributes[string(attName)] = numDistincts;

        Statistics1[string(relName)] = rel_info;

		vector<string> relationName;
		relationName.push_back(string(relName));
		colToRelation[string(attName)] = relationName;
    }
    else {
		map <string, unsigned long long int>::iterator attributesIterator = (statsIterator->second).Attributes.find(string(attName));
		if (attributesIterator == (statsIterator->second).Attributes.end()) {
			(statsIterator->second).Attributes[string(attName)] = numDistincts;
			vector<string> relationName;
	        relationName.push_back(string(relName));
	        colToRelation[string(attName)] = relationName;
		}
		else
			attributesIterator->second = numDistincts;
    }
}


void Statistics::Read(char *fromWhere)
{
	FILE * file = fopen(fromWhere, "r");
	if (file == NULL)
		return;

	string relationName, attributeName;
	unsigned long long int numberOfValues;
	char token[200];
	
    map <string, vector <string> >::iterator columnToTableIterator;

	while (fscanf(file, "%s", token) != EOF) {
		if( strcmp(token, "BEGIN") == 0 ) {
			fscanf(file, "%s", token);
			relationName = token;
			
			RelationInfo relationInfo;
			fscanf(file, "%llu", &relationInfo.totalNumbOfRows);
			
            fscanf(file, "%s", token);
			while (strcmp(token, "END") != 0)
			{
				attributeName = token;
				fscanf(file, "%llu", &numberOfValues);
				relationInfo.Attributes[attributeName] = numberOfValues;

                columnToTableIterator = colToRelation.find(string(attributeName));
                if (columnToTableIterator == colToRelation.end()) { // not found
	                vector<string> tableName;
                    tableName.push_back(string(relationName));
                    colToRelation[string(attributeName)] = tableName;
                }
                else {
    	            (columnToTableIterator->second).push_back(string(relationName));
                }
                                
				fscanf(file, "%s", token);
			}
			Statistics1[relationName] = relationInfo;
		}
	}	
}

void Statistics::CopyRel(char *oldName, char *newName)
{
	map <string, vector <string> >::iterator columnToTableIterator;
	map <string, RelationInfo>::iterator statisticsIterator = Statistics1.find(string(oldName));

	RelationInfo *oldRelInfo = &(statisticsIterator->second);
	RelationInfo relationInfo;
	relationInfo.totalNumbOfRows = oldRelInfo->totalNumbOfRows;

	for (map <string, unsigned long long int>::iterator attributesIterator = oldRelInfo->Attributes.begin(); attributesIterator != oldRelInfo->Attributes.end(); attributesIterator++) {
		relationInfo.Attributes[attributesIterator->first] = attributesIterator->second;

		columnToTableIterator = colToRelation.find(attributesIterator->first);
		if (columnToTableIterator == colToRelation.end()) {
				cout << "\nERROR! Column name " << (attributesIterator->first).c_str() << " not present in columnToRelation! \n";
				return;
		}
		(columnToTableIterator->second).push_back(string(newName));
	}

	Statistics1[string(newName)] = relationInfo;
}

void Statistics::Write(char *toWhere)
{
	FILE * fileToWrite = fopen(toWhere, "w");

	for (map <string, RelationInfo>::iterator stats_itr = Statistics1.begin(); stats_itr != Statistics1.end(); stats_itr++)
	{
		fprintf(fileToWrite, "\nBEGIN");

		fprintf(fileToWrite, "\n%s", stats_itr->first.c_str());
		RelationInfo * relationInfo = &(stats_itr->second);

		fprintf(fileToWrite, "\n%llu", relationInfo->totalNumbOfRows);

		for (map <string, unsigned long long int>::iterator atts_itr = relationInfo->Attributes.begin(); atts_itr != relationInfo->Attributes.end(); atts_itr++) {
			fprintf(fileToWrite, "\n%s", atts_itr->first.c_str());
			fprintf(fileToWrite, " %llu", atts_itr->second);
		}
		fprintf(fileToWrite, "\nEND\n");
	}
	
	fclose(fileToWrite);
}

void  Statistics::Apply(struct AndList *parseTree, char *relNames[], int numToJoin)
{
	double newRowCount = Estimate(parseTree, relNames, numToJoin);
	if (newRowCount == -1) {
		cout << "\nError in Estimate().\n";
		return;
	}
	else {
        set <string> setJoinTables;
		vector<string> joinAttributes;
		if (!validateParseTree(parseTree, relNames, numToJoin, joinAttributes, setJoinTables)) {
			cout << "\nParse Tree is not a valid parse tree.\n";
			return;
		}
		else {
			int oldPartitionNumber = -1;
			set <string>::iterator setIterator = setJoinTables.begin();
			map <string, RelationInfo>::iterator statisticsIterator;
			
            for (; setIterator != setJoinTables.end(); setIterator++) {
				statisticsIterator = Statistics1.find(*setIterator);
				if (statisticsIterator == Statistics1.end()) {
                    cout << "\n ERROR! Details of table " << (*setIterator).c_str() << " are missing! \n";
                    return;
                }	
				if ((statisticsIterator->second).numbOfPartition != -1) {
					oldPartitionNumber = (statisticsIterator->second).numbOfPartition;
					break;
				}
			}

			if (oldPartitionNumber == -1) {
				partitionNumber++;
				vector <string> vTableNames;
	            for (setIterator = setJoinTables.begin(); setIterator != setJoinTables.end(); setIterator++)
    	        {
        	        statisticsIterator = Statistics1.find(*setIterator);
            	    (statisticsIterator->second).numbOfPartition = partitionNumber;
                	(statisticsIterator->second).totalNumbOfRows = (unsigned long long int)newRowCount;

    	            vTableNames.push_back(*setIterator);
        	    }

	            PartInfo[partitionNumber] = vTableNames;
			}
			else {

				vector <string> vTableNames = PartInfo[oldPartitionNumber];
				for (int i=0; i<vTableNames.size(); i++)
					setJoinTables.insert(vTableNames.at(i));

				vTableNames.clear();
				for (setIterator = setJoinTables.begin(); setIterator != setJoinTables.end(); setIterator++)
				{
					statisticsIterator = Statistics1.find(*setIterator);
					(statisticsIterator->second).numbOfPartition = oldPartitionNumber;
					(statisticsIterator->second).totalNumbOfRows = (unsigned long long int)newRowCount;
		
					vTableNames.push_back(*setIterator);
				}
				
				PartInfo[oldPartitionNumber] = vTableNames;
			}
		}
	}
}


bool Statistics::validateParseTree(struct AndList *parseTreeArg, char *relNames[], int numToJoin, vector<string>& joinAttsInPair, set<string>& table_names_set)
{
    for (int i = 0; i < numToJoin; i++) {
        if(Statistics1.find(relNames[i]) == Statistics1.end())
            return false;
    }

	int prefixedTabNamePos;
	string tableName, columnName;
	map<string, vector<string> >::iterator column2TableIterator;

    AndList* parseTree = parseTreeArg;

    while(parseTree != NULL)
    {
        OrList* orsList = parseTree->left;
        while(orsList != NULL)
        {
            ComparisonOp* comparisonOperator = orsList->left;
            if(comparisonOperator == NULL) {
                break;
            }

            int leftCode = comparisonOperator->left->code;
            string leftVal = comparisonOperator->left->value;

			stringstream stream1;
			stream1 << leftCode;

            joinAttsInPair.push_back(stream1.str());
            joinAttsInPair.push_back(leftVal);

			stringstream stream2;
			stream2 << comparisonOperator->code;
            joinAttsInPair.push_back(stream2.str());

            int rightCode = comparisonOperator->right->code;
            string rightVal = comparisonOperator->right->value;

			stringstream stream3;
			stream3 << rightCode;
            joinAttsInPair.push_back(stream3.str());
            joinAttsInPair.push_back(rightVal);

            if(leftCode == NAME)
            {
                prefixedTabNamePos = leftVal.find(".");
                if (prefixedTabNamePos != string::npos)
                {
                    tableName = leftVal.substr(0, prefixedTabNamePos);
                    columnName = leftVal.substr(prefixedTabNamePos + 1);
                	column2TableIterator = colToRelation.find(columnName);
	                if (column2TableIterator == colToRelation.end())
    	                return false;
                }
                else
                {
                    columnName = leftVal;
                	column2TableIterator = colToRelation.find(columnName);
	                if (column2TableIterator == colToRelation.end())
    	                return false;

                    if ((column2TableIterator->second).size() > 1)
    	                return false;
                    else
	                    tableName = (column2TableIterator->second).at(0);
                }
                table_names_set.insert(tableName);
            }
            if(rightCode == NAME)
            {
                prefixedTabNamePos = rightVal.find(".");
                if (prefixedTabNamePos != string::npos)
                {
                    tableName = rightVal.substr(0, prefixedTabNamePos);
                    columnName = rightVal.substr(prefixedTabNamePos + 1);
                	column2TableIterator = colToRelation.find(columnName);
	                if (column2TableIterator == colToRelation.end())
    	                return false;
                }
                else
                {
                    columnName = rightVal;
                	column2TableIterator = colToRelation.find(columnName);
	                if (column2TableIterator == colToRelation.end())
    	                return false;

                    if ((column2TableIterator->second).size() > 1)
                        return false;
                    else
                        tableName = (column2TableIterator->second).at(0);
                }
                table_names_set.insert(tableName);
            }

            if(orsList->rightOr != NULL)
                joinAttsInPair.push_back("OR");
            orsList = orsList->rightOr;
        }
        if(parseTree->rightAnd != NULL)
            joinAttsInPair.push_back("AND");
        else
            joinAttsInPair.push_back(".");
        parseTree = parseTree->rightAnd;
    }

	for (int i=0; i<numToJoin; i++)
	{
		tableName = relNames[i];
		int partitionNum = Statistics1[tableName].numbOfPartition;
		if (partitionNum != -1)
		{
			vector<string> tableNamesVector = PartInfo[partitionNum];
			for (int j = 0; j < tableNamesVector.size(); j++)
			{
				string tab1 = tableNamesVector.at(j);
				bool found = false;
				for (int k = 0; k < numToJoin; k++)
				{
					string table2 = relNames[k];
					if (tab1.compare(table2) == 0)
					{
						found = true;
						break;
					}
				}
				if (found == false){
					return false;
				}
			}
		}
	}

	set <string>::iterator set_itr = table_names_set.begin();
    for (; set_itr != table_names_set.end(); set_itr++)
	{
		string tab1 = *set_itr;
		bool isFound = false;
        for (int k = 0; k < numToJoin; k++)
		{
        	string tab2 = relNames[k];
            if (tab1.compare(tab2) == 0) {
            	isFound = true;
                break;
            }
		}
        if (isFound == false){
        	return false;
        }
	}

    return true;
}


double Statistics::Estimate(struct AndList *parseTree, char **relNames, int numToJoin)
{
    set <string> temp;
    vector<string> joinAttributesPair;
    if ( ! validateParseTree(parseTree, relNames, numToJoin, joinAttributesPair, temp)) {
        cout<< "\nParse tree invalid" << endl;
        return -1;
    }
    
    set <string> setJoinTables;
    vector<long double> estimatesVector;
    map<string, ColumnCountAndEstimate> localOrListEstimates;
    string lastConnectr = "";
    int i = 0;
    while(i < joinAttributesPair.size())
    {
        long double local_Estimate = -1;

        int column1Type = atoi(joinAttributesPair.at(i++).c_str());
        string column1Val = joinAttributesPair.at(i++);
        int operatorCode = atoi(joinAttributesPair.at(i++).c_str());
        int column2Type = atoi(joinAttributesPair.at(i++).c_str());
        string column2Val = joinAttributesPair.at(i++);
        string connectr = joinAttributesPair.at(i++);

        string table1;
        int prefixedTabNamePos;
        if(column1Type == NAME)
        {
            prefixedTabNamePos = column1Val.find(".");
            if(prefixedTabNamePos != string::npos)
            {
                table1 = column1Val.substr(0, prefixedTabNamePos);
                column1Val = column1Val.substr(prefixedTabNamePos + 1);
            }
            else
                table1 = colToRelation[column1Val].at(0);

            setJoinTables.insert(table1);
        }

        string table2;
        if(column2Type == NAME)
        {
            prefixedTabNamePos = column2Val.find(".");
            if(prefixedTabNamePos != string::npos)
            {
                table2 = column2Val.substr(0, prefixedTabNamePos);
                column2Val = column2Val.substr(prefixedTabNamePos + 1);
            }
            else
                table2 = colToRelation[column2Val].at(0);

            setJoinTables.insert(table2);
        }

        if(column1Type == NAME && column2Type == NAME)    //join condition
        {
            RelationInfo t1;
            RelationInfo t2;
            t1 = Statistics1[table1];
            t2 = Statistics1[table2];

            local_Estimate = 1.0/(max(t1.Attributes[column1Val], t2.Attributes[column2Val]));

            estimatesVector.push_back(local_Estimate);
        }
        else if(column1Type == NAME || column2Type == NAME)
        {
            RelationInfo t;
            string columnName;
            if(column1Type == NAME)
            {
                t = Statistics1[table1];
                columnName = column1Val;
            }
            else
            {
                t = Statistics1[table2];
                columnName = column2Val;
            }
            if(operatorCode == EQUALS)
            {
                if(connectr.compare("OR") == 0 || lastConnectr.compare("OR") == 0)
                {
                    if(localOrListEstimates.find(columnName + "=") == localOrListEstimates.end())
                    {
                        local_Estimate = (1.0- 1.0/t.Attributes[columnName]);
                        ColumnCountAndEstimate *colCountEstimate = new ColumnCountAndEstimate();
                        colCountEstimate->repeatCount = 1;
                        colCountEstimate->estimate = local_Estimate;
                        localOrListEstimates[columnName + "="] = *colCountEstimate;
                    }
                    else
                    {
                        local_Estimate = 1.0/t.Attributes[columnName];
                        ColumnCountAndEstimate* colCountEstimate = &(localOrListEstimates[columnName + "="]);
                        colCountEstimate->repeatCount += 1;
                        colCountEstimate->estimate = colCountEstimate->repeatCount*local_Estimate;
                    }
                    if(connectr.compare("OR") != 0)
                    {
                        long double tempResult = 1.0;
                        map<string, ColumnCountAndEstimate>::iterator it = localOrListEstimates.begin();
                        for(; it != localOrListEstimates.end(); it++)
                        {
                            if(it->second.repeatCount == 1)
                                tempResult *= it->second.estimate;
                            else
                                tempResult *= (1 - it->second.estimate);
                        }

                        long double totalCurrentEstimate = 1.0 - tempResult;
                        estimatesVector.push_back(totalCurrentEstimate);

                        localOrListEstimates.clear();                        
                    }
                }
                else
                {
                    local_Estimate = 1.0/t.Attributes[columnName];
                    estimatesVector.push_back(local_Estimate);
                }
            }
            else
            {
                if(connectr.compare("OR") == 0 || lastConnectr.compare("OR") == 0)
                {
                    local_Estimate = (1.0 - 1.0/3);
					
                    ColumnCountAndEstimate *cce = new ColumnCountAndEstimate();
                    cce->repeatCount = 1;
                    cce->estimate = local_Estimate;
                    localOrListEstimates[columnName] = *cce;

                    if(connectr.compare("OR") != 0)
                    {
						long double tempResult = 1.0;
                        map<string, ColumnCountAndEstimate>::iterator it = localOrListEstimates.begin();
                        for(; it != localOrListEstimates.end(); it++)
                        {
                            if(it->second.repeatCount == 1)
                                tempResult *= it->second.estimate;
                            else
                                tempResult *= (1 - it->second.estimate);
                        }

                        long double totalCurrentEstimate = 1.0 - tempResult;
                        estimatesVector.push_back(totalCurrentEstimate);
						
                        localOrListEstimates.clear();
                    }
                }
                else
                {
                    local_Estimate = (1.0/3);
                    estimatesVector.push_back(local_Estimate);
                }
            }
        }
        else
        {

        }
        lastConnectr = connectr;
    }
    
    unsigned long long int numerator = 1;
    set <string>::iterator setIterator = setJoinTables.begin();
    for (; setIterator != setJoinTables.end(); setIterator++)
        numerator *= Statistics1[*setIterator].totalNumbOfRows;

    double result = numerator;
    for(int i = 0; i < estimatesVector.size(); i++)
    {
        result *= estimatesVector.at(i);
    }
    return result;
}

map<int, vector<string> >* Statistics::GetPartsInfo()
{
    return &PartInfo;
}
	
map<string, RelationInfo>* Statistics::GetStatistics()
{
    return &Statistics1;
}
	
int Statistics::GetNumberOfPartition()
{
    return partitionNumber;
}
