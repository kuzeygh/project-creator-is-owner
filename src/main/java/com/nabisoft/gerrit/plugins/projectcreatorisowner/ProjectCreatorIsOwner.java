package com.nabisoft.gerrit.plugins.projectcreatorisowner;

import java.io.IOException;

import org.eclipse.jgit.errors.ConfigInvalidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gerrit.common.data.AccessSection;
import com.google.gerrit.common.data.GroupDescription;
import com.google.gerrit.common.data.GroupReference;
import com.google.gerrit.common.data.Permission;
import com.google.gerrit.common.data.PermissionRule;
import com.google.gerrit.extensions.events.NewProjectCreatedListener;
import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.reviewdb.client.AccountGroup;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.account.GroupBackend;
import com.google.gerrit.server.git.MetaDataUpdate;
import com.google.gerrit.server.git.ProjectConfig;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;


class ProjectCreatorIsOwner implements NewProjectCreatedListener {
	
	private static final Logger log = LoggerFactory.getLogger(ProjectCreatorIsOwner.class);

	public static class Module extends AbstractModule {
		@Override
		protected void configure() {
			DynamicSet.bind(binder(), NewProjectCreatedListener.class).to(ProjectCreatorIsOwner.class);
		}
	}

	private final MetaDataUpdate.User metaDataUpdateFactory;
	private final GroupBackend groupBackend;
	private final Provider<IdentifiedUser> identifiedUser;
	
	@Inject
	ProjectCreatorIsOwner(MetaDataUpdate.User metaDataUpdateFactory, GroupBackend groupBackend, Provider<IdentifiedUser> identifiedUser) {
		this.metaDataUpdateFactory	= metaDataUpdateFactory;
		this.groupBackend			= groupBackend;
		this.identifiedUser			= identifiedUser;
	}

	@Override
	public void onNewProjectCreated(NewProjectCreatedListener.Event event) {
		
		String projectName = event.getProjectName();
		Project.NameKey p = new Project.NameKey(projectName);
		
		try {
			MetaDataUpdate md = metaDataUpdateFactory.create(p);
			ProjectConfig config = ProjectConfig.read(md);
			AccessSection all = config.getAccessSection(AccessSection.ALL, true);
			IdentifiedUser identUser = identifiedUser.get();
			
			AccountGroup.UUID ownerId = new AccountGroup.UUID("user:" + identUser.getUserName());
			GroupDescription.Basic g = groupBackend.get(ownerId);
			
			if(g != null) {
				GroupReference group = config.resolve(GroupReference.forGroup(g));
				all.getPermission(Permission.OWNER, true).add(new PermissionRule(group));
				String msg = String.format("Make project creator '%s' owner of project '%s'", identUser.getUserName(), projectName);
				md.setMessage(msg + "\n");
				config.commit(md);
				log.info(msg);
			} else {
				log.warn("singleusergroup plugin seems not to be installed, thus can't make project creator a project owner.");
			}
			
		} catch (IOException | ConfigInvalidException e) {
			log.error("Failed to make project creator a project owner.", e);
		}

	}

}
