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
	
	// @formatter:off
	private List<String> operations = List.of( // This is the menu.
		"1) Add a Project"
			);
	// @formatter:on
	
	public static void main(String[] args) { // main method
		//DbConnection.getConnection();
		new ProjectsApp().processUserSelections();
	
	} // main
	
	private void processUserSelections() {
		boolean done = false;
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
				default:
					System.out.println("\n" + selection + " is not a valid selection.  Try again.");
					break;
				} // switch
			} catch (Exception e) {
				System.out.println("\nError:  " + e + " - Try again."); // If the user inputs something not on the menu...
			} // try-catch
		} // while

	} // processUserSelections method

	private void createProject() {
		//ProjectsApp projectName; // associated with 2 lines below...
		String projectName = getStringInput("Enter the project name"); 	// Collecting data for the new project...
		//projectName.getStringInput("Enter the project name"); // Another way of doing this?
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
	}

} // class
