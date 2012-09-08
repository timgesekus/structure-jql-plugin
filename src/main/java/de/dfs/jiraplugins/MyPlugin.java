package de.dfs.jiraplugins;

import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.plugin.jql.function.AbstractJqlFunction;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

public class MyPlugin extends AbstractJqlFunction
{

	@Override
	public JiraDataType getDataType() {
		return JiraDataTypes.ISSUE;
	}

	@Override
	public int getMinimumNumberOfExpectedArguments() {
		return 1;
	}

	@Override
	public List<QueryLiteral> getValues(QueryCreationContext arg0,
			FunctionOperand arg1, TerminalClause arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageSet validate(User arg0, FunctionOperand arg1,
			TerminalClause arg2) {
		// TODO Auto-generated method stub
		return null;
	}
}
