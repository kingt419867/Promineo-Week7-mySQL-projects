package projects.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import projects.dao.ProjectDao;
import projects.entity.Project;
import projects.exception.DbException;

public class ProjectService {

	private ProjectDao projectDao = new ProjectDao();
	
	public Project addProject(Project project) {
		return projectDao.insertProject(project);
	}

	public List<Project> fetchAllProjects() {
		//return projectDao.fetchAllProjects();
		return projectDao.fetchAllProjects().stream().sorted((p1, p2) -> p1.getProjectId() - p2.getProjectId()).collect(Collectors.toList()); // This sorts the list by projectId.
	} // fetchAllProjects

	public Project fetchProjectById(Integer projectId) {
		//Optional<Project> op = projectDao.fetchProjectById(projectId);
		return projectDao.fetchProjectById(projectId).orElseThrow(() -> new NoSuchElementException("Project with project ID " + projectId + ", does not exist."));
	} //fetchProjectById

	public void modifyProjectDetails(Project project) {
		System.out.println("Attempting update of project " + project.getProjectId());
		if(!projectDao.modifyProjectDetails(project)) {
			throw new DbException("Project " + project.getProjectId() + " does not exist.");
		} else {
			System.out.println("Update of project " + project.getProjectId() + " successful.");
		} // if
	} // modifyProjectDetails

	public void deleteProject(Project project) {
		System.out.println("Attempting deletion of project " + project.getProjectId());
		if(!projectDao.deleteProcject(project)) {
			throw new DbException("Project " + project.getProjectId() + " does not exist.");
		} else {
			System.out.println("Deletion of project " + project.getProjectId() + " successful - from service.");
		} // if
	} // deleteProject

} // class
