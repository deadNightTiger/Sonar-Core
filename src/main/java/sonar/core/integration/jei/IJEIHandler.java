package sonar.core.integration.jei;

import java.util.ArrayList;

import mezz.jei.api.IGuiHelper;
import sonar.core.helpers.RecipeHelper;

public interface IJEIHandler {
	
	public JEICategory getCategory(IGuiHelper guiHelper);
	
	public String getTextureName();
	
	public String getTitle();
	
	public Class getRecipeClass();
	
	public RecipeHelper getRecipeHelper();
	
	public ArrayList<JEIRecipe> getJEIRecipes();
}