package de.dfs.jiraplugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.almworks.jira.structure.api.PermissionLevel;
import com.almworks.jira.structure.api.Structure;
import com.almworks.jira.structure.api.StructureManager;
import com.almworks.jira.structure.api.StructureServices;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.plugin.jql.function.AbstractJqlFunction;
import com.atlassian.jira.plugin.jql.function.JqlFunctionModuleDescriptor;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.plugin.PluginAccessor.Descriptor;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

public class MyPlugin extends AbstractJqlFunction {

	private VersionManager versionManager;
	private IssueManager issueManager;
	private JqlFunctionModuleDescriptor descriptor;
	private StructureManager structureManager;

	public MyPlugin(VersionManager versionManager, IssueManager issueManager,
			StructureServices structureServices) {
		this.versionManager = versionManager;
		this.issueManager = issueManager;
		this.structureManager = structureServices.getStructureManager();
	}

	@Override
	public boolean isList() {
		return true;
	}

	@Override
	public String getFunctionName() {
		return ("versionStructure");
	}

	@Override
	public void init(final JqlFunctionModuleDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public JiraDataType getDataType() {
		return JiraDataTypes.ISSUE;
	}

	@Override
	public int getMinimumNumberOfExpectedArguments() {
		return 1;
	}

	@Override
	public List<QueryLiteral> getValues(QueryCreationContext context,
			FunctionOperand functionOperand, TerminalClause arg2) {
		List<Structure> structures = structureManager.getStructuresByName(null, context.getUser(), PermissionLevel.VIEW, false);
		final List<QueryLiteral> literals = new ArrayList<QueryLiteral>();
		Collection<Version> versions = getVersions(functionOperand);
		for (Version version : versions) {
			Collection<Issue> issues = versionManager
					.getIssuesWithFixVersion(version);
			for (Issue issue : issues) {
				literals.add(new QueryLiteral(functionOperand, issue.getId()));
			}
		}
		return literals;
	}

	private List<Long> getStructureChildren(Long issueId, Structure structure) {
		List<Long> children = new ArrayList<Long>();
		//Forest forest = structure.getForest(arg0, arg1)
		return children;
	}
	private Collection<Version> getVersions(FunctionOperand functionOperand) {
		List<String> args = functionOperand.getArgs();
		Collection<Version> versions = versionManager.getVersionsByName(args
				.get(0));
		return versions;
	}

	@Override
	public MessageSet validate(User searcher, FunctionOperand functionOperand,
			TerminalClause terminalClause) {
		MessageSet messages = new MessageSetImpl();
		List<String> args = functionOperand.getArgs();
		if (args.size() != 1) {
			messages.addErrorMessage(descriptor.getI18nBean().getText(
					"rolefunc.bad.num.arguments", functionOperand.getName()));
		} else {
			int numberOfVersions = getVersions(functionOperand).size();
			if (numberOfVersions == 0) {
				messages.addErrorMessage(descriptor.getI18nBean().getText(
						"Version not found", functionOperand.getName()));
			}
		}
		return messages;
	}

}
