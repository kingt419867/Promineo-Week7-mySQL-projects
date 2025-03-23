package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;

public class ProjectDao extends DaoBase {

	private static final String CATEGORY_TABLE = "category";
	private static final String MATERIAL_TABLE = "material";
	private static final String PROJECT_TABLE = "project";
	private static final String PROJECT_CATEGORY_TABLE = "project_category";
	private static final String STEP_TABLE = "step";
	
	public Project insertProject(Project project) {
		//@formatter:off
		String sql = ""
				+ "INSERT INTO " + PROJECT_TABLE + " "
				+ "(project_name, estimated_hours, actual_hours, difficulty, notes) "
				+ "VALUES "
				+ "(?, ?, ?, ?, ?)";
		//@formatter:on
		
		try(Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			try(PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);
				
				stmt.executeUpdate();
				Integer projectId = getLastInsertId(conn, PROJECT_TABLE); // This gets the last auto_increment value for projectId.
				commitTransaction(conn);
				project.setProjectId(projectId); // This sets the current projectId.
				return project;
			} catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			} // inner try-catch
		} catch(SQLException e) {
			throw new DbException(e);
		} // try-catch
	} // insertProject

	public boolean modifyProjectDetails(Project project) {
		//@formatter:off
		String sql = ""
				+ "UPDATE " + PROJECT_TABLE + " SET "
				+ "project_name = ?, estimated_hours = ?, actual_hours = ?, difficulty = ?, notes = ? "
				+ "WHERE project_id = ?";
		//@formatter:on
		
		try(Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			System.out.println("Update transaction started for Project " + project.getProjectId());
			try(PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);
				setParameter(stmt, 6, project.getProjectId(), Integer.class); // Included to set the WHERE clause in the SQL statement.
				boolean updated = stmt.executeUpdate() == 1; // This checks to see how many rows the executeUpdate updated.  If it only updated 1 row, it returns true for updated, which then gets returned for modifyProjectDetails.
				System.out.println("During attempt of Project " + project.getProjectId() + " it updated " + updated + " records.");
				commitTransaction(conn);
				return updated;
			} catch(Exception e) {
				rollbackTransaction(conn);
				System.out.println("Update failed");
				throw new DbException(e);
			} // inner try-catch
		} catch(SQLException e) {
			throw new DbException(e);
		} // try-catch
	} // modifyProjectDetails

	public boolean deleteProcject(Project project) {
		//@formatter:off
		String sql = "DELETE FROM " + PROJECT_TABLE
				+ " WHERE project_id = ?";
		//@formatter:on
		try(Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			System.out.println("Starting deletion of Project " + project.getProjectId());
			try(PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, project.getProjectId(), Integer.class); // Included to set the WHERE clause in the SQL statement.
				boolean deleted = stmt.executeUpdate() == 1; // This checks to see how many rows the executeUpdate updated.  If it only updated 1 row, it returns true for updated, which then gets returned for modifyProjectDetails.
				commitTransaction(conn);
				System.out.println("Delete successful - from DAO");
				return deleted;
			} catch(Exception e) {
				rollbackTransaction(conn);
				System.out.println("Delete failed");
				throw new DbException(e);
			} // inner try-catch
		} catch(SQLException e) {
			throw new DbException(e);
		} // try-catch
	} // deleteProcject

	
	public List<Project> fetchAllProjects() { // This will list all the projects.
		//@formatter:off
		String sql = "SELECT * FROM " // This is the SQL statement for listing them.
				 + PROJECT_TABLE + " ORDER BY project_Name";
		//@formatter:on
		try(Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			try(PreparedStatement stmt = conn.prepareStatement(sql)) { // The SQL statement gets passed to the DB here.
				try(ResultSet rs = stmt.executeQuery()) {
					List<Project> projects = new LinkedList<>();
					while(rs.next()) {
						projects.add(extract(rs, Project.class));
					}
					return projects;
				} // single try, 3rd inner try //and it is executed here.
			} catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			} // inner try-catch
		} catch(SQLException e) {
			throw new DbException(e);
		} // try-catch
	} // fetchAllProjects

	public Optional<Project> fetchProjectById(Integer projectId) {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?";
		try(Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			try {
				Project project = null;
				try(PreparedStatement stmt = conn.prepareStatement(sql)) {
					setParameter(stmt, 1, projectId, Integer.class);
					try(ResultSet rs = stmt.executeQuery()) {
						if (rs.next()) {
							project = extract(rs, Project.class);
						} // ResultSet if
					} // single try, 3rd inner try
					if (Objects.nonNull(project)) {
						project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
						project.getSteps().addAll(fetchStepsForProject(conn, projectId));
						project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
					} // stmt if
					commitTransaction(conn);
					return Optional.ofNullable(project);
				} // single try, 2nd inner try
			} catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			} // First Inner try-catch
		} catch(SQLException e) {
			throw new DbException(e);
		} // try-catch
	} // fetchProjectById

	private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId) throws SQLException {
		//@formatter:off
		String sql = "SELECT * FROM " + MATERIAL_TABLE + " m WHERE m.project_id = ?";
		//@formatter:on
		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);
			try(ResultSet rs = stmt.executeQuery()) {
				List<Material> materials = new LinkedList<Material>();
				while(rs.next()) {
					Material material = extract(rs, Material.class);
					materials.add(material);
				} // while
				return materials;
			} // Inner try
		} // try
	} // fetchMaterialsForProject

	private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {
		String sql = "SELECT * FROM " + STEP_TABLE + " s WHERE s.project_id = ?"; 
		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			setParameter(stmt, 1, projectId, Integer.class);
			try(ResultSet rs = stmt.executeQuery()) {
				List<Step> steps = new LinkedList<Step>();
				while(rs.next()) {
					steps.add(extract(rs, Step.class));
				} // while
				return steps;
				} // ResultSet if
		} // try
	} // fetchStepsForProject

	private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) throws SQLException {
		//@formatter:off
			String sql = ""
				+ "SELECT c.* FROM " + CATEGORY_TABLE + " c "
				+ "JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
				+ "WHERE project_id = ?";
		//@formatter:on
			try(PreparedStatement stmt = conn.prepareStatement(sql)) {
				setParameter(stmt, 1, projectId, Integer.class);
				try(ResultSet rs = stmt.executeQuery()) {
					List<Category> categories = new LinkedList<>();
					while(rs.next()) {
						categories.add(extract(rs, Category.class));
					} // while
					return categories;
				} // inner try
			} // try
	} // fetchCategoriesForProject


} // class
