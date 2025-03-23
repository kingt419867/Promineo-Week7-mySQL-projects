package projects;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import projects.entity.Project;
import projects.exception.DbException;
import projects.service.ProjectService;

//import projects.dao.DbConnection;

public class ProjectsApp {

	private Scanner scanner = new Scanner(System.in); // scanner setup
	private ProjectService projectService = new ProjectService();
	private Project curProject = new Project();
	
	// @formatter:off
	private List<String> operations = List.of( // This is the menu.
		"1) Add a Project",
		"2) List Projects",
		"3) Display a Project",
		"4) Update project details",
		"5) Delete a project"
			);
	// @formatter:on
	
	public static void main(String[] args) { // main method
		//DbConnection.getConnection();
		new ProjectsApp().processUserSelections();
	
	} // main
	
	private void processUserSelections() {
		boolean done = false;
		curProject = null; // Adding this got rid of the initial project readout, where there wasn't any project selected, but curProject still wasn't null. Week 11, 3/22/25
		while(!done) {						// This operates the menu.
			try {
				int selection = getUserSelection(); // method to grab user's selection.
				switch(selection) {
				case -1:
					done = exitMenu();
					break;
				case 1:
					createProject();
					break;
				case 2:
					listProjects();
					break;
				case 3:
					selectProject();
					break;
				case 4:
					updateProjectDetails();
					break;
				case 5:
					deleteProject();
					break;
				default:
					System.out.println("\n" + selection + " is not a valid selection.  Try again.");
					break;
				} // switch
			} catch (Exception e) {
				System.out.println("\nError:  " + e + " - Try again."); // If the user inputs something not on the menu...
			} // try-catch
		} // while

	} // processUserSelections method

	private void deleteProject() {
		listProjects();
		Integer projectId = getIntInput("Enter the project ID of the project you want to delete:  ");
		curProject = null; // This deselects any previously selected current project, to avoid problems if an exception is thrown.
		curProject = projectService.fetchProjectById(projectId);
		if(Objects.isNull(curProject)) {
			System.out.println("\nInvalid Project ID.  Please select one that is listed.");
		} // if
		Project project = new Project();
		project.setProjectId(curProject.getProjectId());
		String verifyDelete = getStringInput("\nYou are about to delete project " + project.getProjectId() + ". Type Yes to continue, or No to abort.");
		System.out.println(verifyDelete);
		if(verifyDelete.equals("Yes")) {
			projectService.deleteProject(project);
			listProjects();
		} else {
			System.out.println("\nAborting...\n");
			listProjects();
			curProject = null;
		} // verifyDelete if
		curProject = null;
	} // deleteProject

	private void updateProjectDetails() {
		listProjects();
		Integer projectId = getIntInput("Enter a project ID to select a project");
		curProject = null; // This deselects any previously selected current project, to avoid problems if an exception is thrown.
		curProject = projectService.fetchProjectById(projectId);
		if(Objects.isNull(curProject)) {
			System.out.println("\nInvalid Project ID.  Please select one that is listed.");
		} // if
		// The below recalls the current project name, as well as the other project fields, and asks whether you want to change them.
		String projectName = getStringInput("Update the project name, or hit ENTER to keep the current name:  [" + curProject.getProjectName() + "]");
		BigDecimal estimatedHours = getDecimalInput("Update the estimated hours, or hit ENTER to keep the current estimated hours:  [" + curProject.getEstimatedHours() + "]");
		BigDecimal actualHours = getDecimalInput("Update the actual hours, or hit ENTER to keep the current actual hours:  [" + curProject.getActualHours() + "]");
		Integer difficulty = getIntInput("Update the project difficulty (1-5), or hit ENTER to keep the current difficulty:  [" + curProject.getDifficulty() + "]");
		String notes = getStringInput("Update the project notes, or hit ENTER to keep the current notes:  [" + curProject.getNotes() + "]");
		
		Project project = new Project(); // Based on the answers, above, this part saves the responses to the respective fields.
		project.setProjectName(Objects.isNull(projectName) ? curProject.getProjectName() : projectName);
		project.setEstimatedHours(Objects.isNull(estimatedHours) ? curProject.getEstimatedHours() : estimatedHours);
		project.setActualHours(Objects.isNull(actualHours) ? curProject.getActualHours() : actualHours);
		project.setDifficulty(Objects.isNull(difficulty) ? curProject.getDifficulty() : difficulty);
		project.setNotes(Objects.isNull(notes) ? curProject.getNotes() : notes);
		project.setProjectId(curProject.getProjectId());
		
		// In a real project, this is where I would print out the changes that were just requested, and then ask the user to verify if that's what they really wanted to do, or not, before executing the transaction.
		
		projectService.modifyProjectDetails(project);
		curProject = projectService.fetchProjectById(curProject.getProjectId());
	} // updateProjectDetails

	private void selectProject() {
		listProjects();
		Integer projectId = getIntInput("Enter a project ID to select a project");
		curProject = null; // This deselects any previously selected current project, to avoid problems if an exception is thrown.
		curProject = projectService.fetchProjectById(projectId);
		if(Objects.isNull(curProject)) {
			System.out.println("\nInvalid Project ID");
		} // if
	} //selectProject

	private void listProjects() {
		List<Project> projects = projectService.fetchAllProjects();
		System.out.println("\nProjects:  ");
		projects.forEach(project -> System.out.println("   " + project.getProjectId() + ": " + project.getProjectName()));
	} // listProjects

	private void createProject() {
		//ProjectsApp projectName; // associated with 2 lines below...
		String projectName = getStringInput("Enter the project name"); 	// Collecting data for the new project...
		BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours");
		BigDecimal actualHours = getDecimalInput("Enter the actual hours");
		Integer difficulty = getIntInput("Enter the project difficulty (1-5)");
		String notes = getStringInput("Enter the project notes");
		
		Project project = new Project(); // This instantiates the new project.
		
		project.setProjectName(projectName);
		project.setEstimatedHours(estimatedHours);
		project.setActualHours(actualHours);
		project.setDifficulty(difficulty);
		project.setNotes(notes);
		
		Project dbProject = projectService.addProject(project); // This passes the project to the database.
		System.out.println("You have successfully created project " + dbProject);
	} // createProject

	private BigDecimal getDecimalInput(String prompt) {
		String input = getStringInput(prompt);
		if (Objects.isNull(input)) { // If the input is blank, this will return null.
			return null;
		} // if
		try {
			return new BigDecimal(input).setScale(2); // This converts the user's time input to decimal, setting the decimal to 2 places.
		} catch(NumberFormatException e) {	// If the user's input cannot be converted to decimal, this handles the error.
			throw new DbException(input + " is not a valid decimal number.");
		} // try-catch
	} // getDecimalInput

	private boolean exitMenu() {
		System.out.println("Exiting the application.");
		return true;
	}

	private int getUserSelection() {
		printOperations();
		Integer input = getIntInput("Enter a menu selection:  ");
		return Objects.isNull(input) ? -1 : input;		// If the input was null, this will instead return a -1 to the switch, which will cause the variable done to turn true, which will exit the program.
	} // getUserSelection

	private Integer getIntInput(String prompt) { // Method to convert whatever the user inputs into an int.
		String input = getStringInput(prompt);
		if (Objects.isNull(input)) { // If the input is blank, this will return null.
			return null;
		} // if
		try {
			return Integer.valueOf(input); // This returns the user's int menu selection.
		} catch(NumberFormatException e) {	// If the user's input cannot be converted to an int, this handles the error.
			throw new DbException(input + " is not a valid number.");
		} // try-catch
		
	} // getIntInput

	private String getStringInput(String prompt) { // The user input comes in as a string, here.
		System.out.print(prompt + " ");
		String input = scanner.nextLine(); // This collects input from the scanner.
		return input.isBlank() ? null : input.trim(); // If the input is null, it returns null; otherwise, it trims any trailing spaces and returns the input. 
	} // getStringInput

	private void printOperations() {
		System.out.println("\nThese are the available options:  (Press ENTER to quit.)"); // This prints out the menu options.
		operations.forEach(op -> System.out.println("   " + op));
		if(Objects.isNull(curProject)) {
			System.out.println("\nYou are not working with a project.  Please select a project, first.");
		} else {
			System.out.println("\nYou are working with project: " + curProject);
		} // if
	} // printOperations

} // class
