package de.dfs.jiraplugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.almworks.integers.LongArray;
import com.almworks.jira.structure.api.PermissionLevel;
import com.almworks.jira.structure.api.Structure;
import com.almworks.jira.structure.api.StructureException;
import com.almworks.jira.structure.api.StructureManager;
import com.almworks.jira.structure.api.StructureServices;
import com.almworks.jira.structure.api.forest.Forest;
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

	 private static final Logger log = LoggerFactory.getLogger(MyPlugin.class);
	 
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
		
		Set<Long> versionIds = getIssuesForVersions(functionOperand);
		List<Long> structureIssueIds = getStructureChildrenIssues(context,
				versionIds);
		versionIds.addAll(structureIssueIds);
		final List<QueryLiteral> literals = createQueryLiteralsFromIssues(
				functionOperand, versionIds);
		return literals;
	}

	private List<QueryLiteral> createQueryLiteralsFromIssues(
			FunctionOperand functionOperand, Set<Long> versionIds) {
		final List<QueryLiteral> literals = new ArrayList<QueryLiteral>();
		for(Long IssueId: versionIds) {
			literals.add(new QueryLiteral(functionOperand, IssueId));
					
		}
		return literals;
	}

	private List<Long> getStructureChildrenIssues(QueryCreationContext context,
			Set<Long> versionIds) {
		List<Structure> structures = structureManager.getStructuresByName("Global Structure", context.getUser(), PermissionLevel.VIEW, false);
		List<Long> structureIssueIds = new ArrayList<Long>();
		try {
			structureIssueIds = getStructureChildren(structures, new ArrayList<Long> (versionIds), context.getUser());
		} catch (StructureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return structureIssueIds;
	}

	private Set<Long> getIssuesForVersions(FunctionOperand functionOperand) {
		List<Version> versions = getVersions(functionOperand);
		Set<Long> versionIds = getIssuesInVersions(versions);
		return versionIds;
	}

	private Set<Long> getIssuesInVersions(List<Version> versions) {
		Set<Long> versionIds = new HashSet<Long>();
		for (Version version : versions) {
			Collection<Issue> issues = versionManager
					.getIssuesWithFixVersion(version);
			for (Issue issue : issues) {
				versionIds.add(issue.getId());
			}
		}
		return versionIds;
	}

	private List<Long> getStructureChildren(List<Structure> structures, List<Long> issueIds, User user) throws StructureException {
		log.error("Entering getStructureChildren ");
		
		Set<Long> issues = new HashSet<Long>();
		for (Structure structure: structures) {
			log.error("Entering Structure " + structure.getName());
			Forest forest = structure.getForest(user, false);
			for (Long issueId: issueIds) {
				List<Long> childrenIssues = getIssueChildrenInForest(issueId,forest);
				issues.addAll(childrenIssues);
			}
		}
		return new ArrayList<Long>(issues);
	}
	
	private List<Long> getIssueChildrenInForest(Long issueId, Forest forest) {
		Forest subtree = forest.copySubtree(issueId);
		return subtree.getIssues().toList();
	}

	private List<Version> getVersions(FunctionOperand functionOperand) {
		List<String> args = functionOperand.getArgs();
		Collection<Version> versions = versionManager.getVersionsByName(args
				.get(0));
		return new ArrayList<Version>(versions);
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
