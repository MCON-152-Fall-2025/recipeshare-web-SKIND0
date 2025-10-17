package com.mcon152.recipeshare;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcon152.recipeshare.web.RecipeController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecipeController.class)
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = new ObjectMapper();
    }

    // Internal class for creation-related tests
    @Nested
    class CreationTests {
        @Test
        void testAddRecipe() throws Exception {

            ObjectNode json = mapper.createObjectNode();
            json.put("title", "Cake");
            json.put("description", "Delicious cake");
            // Change ingredients to a single String
            json.put("ingredients", "1 cup of flour, 1 cup of sugar, 3 eggs");
            json.put("instructions", "Mix and bake");
            String jsonString = mapper.writeValueAsString(json);
            mockMvc.perform(post("/api/recipes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Cake"))
                    .andExpect(jsonPath("$.description").value("Delicious cake"))
                    .andExpect(jsonPath("$.ingredients").value("1 cup of flour, 1 cup of sugar, 3 eggs"))
                    .andExpect(jsonPath("$.instructions").value("Mix and bake"))
                    .andExpect(jsonPath("$.id").isNumber());
        }

        @ParameterizedTest
        @CsvSource({
                "'Chocolate Cake','Rich chocolate cake','2 cups flour;1 cup cocoa;4 eggs','Bake at 350F for 30 min'",
                "'Pasta Salad','Fresh pasta salad','200g pasta;100g tomatoes;50g olives','Mix all ingredients'",
                "'Pancakes','Fluffy pancakes','1 cup flour;2 eggs;1 cup milk','Cook on skillet until golden'"
        })
        void parameterizedAddRecipeTest(String title, String description, String ingredients, String instructions) throws Exception {
            //throw new UnsupportedOperationException("parameterizedAddRecipeTest");
            //i basically copied the format from TestAddRecipe.
            ObjectNode json = mapper.createObjectNode();
            json.put("title", title);
            json.put("description", description);
            json.put("ingredients", ingredients);
            json.put("instructions", instructions);

            String jsonString = mapper.writeValueAsString(json);

            mockMvc.perform(post("/api/recipes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value(title)) //check the title matches
                    .andExpect(jsonPath("$.description").value(description)) //and so on
                    .andExpect(jsonPath("$.ingredients").value(ingredients))
                    .andExpect(jsonPath("$.instructions").value(instructions))
                    .andExpect(jsonPath("$.id").isNumber());
        }
    }

    // Internal class for delete and get tests
    @Nested
    class DeleteAndGetTests {
        private List<Integer> recipeIds;

        @BeforeEach
        void createRecipes() throws Exception {
            recipeIds = new ArrayList<>();
            String[] recipes = {
                    "{\"title\":\"Pie\",\"description\":\"Apple pie\",\"ingredients\":\"Apples, Flour, Sugar\",\"instructions\":\"Mix and bake\"}",
                    "{\"title\":\"Soup\",\"description\":\"Tomato soup\",\"ingredients\":\"Tomatoes, Water, Salt\",\"instructions\":\"Boil and blend\"}"
            };
            for (String json : recipes) {
                String response = mockMvc.perform(post("/api/recipes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();
                int id = mapper.readTree(response).get("id").asInt();
                recipeIds.add(id);
            }
        }

        @Test
        void testGetAllRecipes() throws Exception {
            mockMvc.perform(get("/api/recipes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("Pie"))
                    .andExpect(jsonPath("$[1].title").value("Soup"));
        }

        @Test
        void testGetRecipe() throws Exception {
            int id = recipeIds.get(0);
            mockMvc.perform(get("/api/recipes/" + id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Pie"));
        }

        @Test
        void testDeleteRecipe()  throws Exception {
            //throw new UnsupportedOperationException("testDeleteRecipe not implemented");
            //create a recipe so similar to CreateRecipes
            String json = "{\"title\":\"Toast\",\"description\":\"Simple toast\",\"ingredients\":\"Bread\",\"instructions\":\"Toast it\"}";

            String response = mockMvc.perform(post("/api/recipes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            int id = mapper.readTree(response).get("id").asInt();

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/recipes/" + id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(true));
        }

        @Test
        void testPutRecipe() throws Exception{
            //throw new UnsupportedOperationException("testPutRecipe not implemented");
            //first og recipe
            String originalJson = "{\"title\":\"Toast\",\"description\":\"Simple toast\",\"ingredients\":\"Bread\",\"instructions\":\"Toast it\"}";

            String response = mockMvc.perform(post("/api/recipes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(originalJson))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            int id = mapper.readTree(response).get("id").asInt();

            //update recipe
            String updatedJson = "{\"title\":\"Avocado Toast\",\"description\":\"Fancy toast\",\"ingredients\":\"Bread, Avocado\",\"instructions\":\"Toast bread, add avocado\"}";

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/recipes/" + id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updatedJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Avocado Toast"))
                    .andExpect(jsonPath("$.description").value("Fancy toast"))
                    .andExpect(jsonPath("$.ingredients").value("Bread, Avocado"))
                    .andExpect(jsonPath("$.instructions").value("Toast bread, add avocado"));
        }

        @Test
        void testPatchRecipe() throws Exception{
            //throw new UnsupportedOperationException("testPatchRecipe not implemented");
            //create an og recipe
            String originalJson = "{\"title\":\"Toast\",\"description\":\"Simple toast\",\"ingredients\":\"Bread\",\"instructions\":\"Toast it\"}";

            String response = mockMvc.perform(post("/api/recipes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(originalJson))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            int id = mapper.readTree(response).get("id").asInt();
            //update it partially.
            String partialJson = "{\"title\":\"Avocado Toast\",\"description\":\"Simple toast\",\"ingredients\":\"Bread\",\"instructions\":\"Toast it\"}";


            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/recipes/" + id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(partialJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Avocado Toast"))
                    .andExpect(jsonPath("$.description").value("Simple toast"))
                    .andExpect(jsonPath("$.ingredients").value("Bread"))
                    .andExpect(jsonPath("$.instructions").value("Toast it"));

            //there was a problem with this program, it was creating a new Recipe, where the rest of the values became null.
            //so that's why the string partrialJson is really long.
        }
    }

    @Nested
    class NonExistingRecipeTests {

        @Test
        void testGetNonExistingRecipe() throws Exception {
            // Skeleton: Try to get a recipe with a non-existing ID
            // Example: mockMvc.perform(get("/api/recipes/9999"))...
            //throw new UnsupportedOperationException("testGetNonExistingRecipe not implemented");
            mockMvc.perform(get("/api/recipes/9999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").doesNotExist());
        }

        @Test
        void testPutNonExistingRecipe() throws Exception {
            // Skeleton: Try to update a recipe with a non-existing ID
            // Example: mockMvc.perform(put("/api/recipes/9999"))...
            //throw new UnsupportedOperationException("testPutNonExistingRecipe not implemented");
            String updatedJson = "{\"title\":\"Ghost Toast\",\"description\":\"Invisible toast\",\"ingredients\":\"Air\",\"instructions\":\"Imagine it\"}";

            mockMvc.perform(put("/api/recipes/9999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updatedJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").doesNotExist());

        }

        @Test
        void testPatchNonExistingRecipe() throws Exception {
            // Skeleton: Try to patch a recipe with a non-existing ID
            // Example: mockMvc.perform(patch("/api/recipes/9999"))...
            //throw new UnsupportedOperationException("testPatchNonExistingRecipe not implemented");
            String patchJson = "{\"title\":\"Ghost Toast\",\"description\":\"Invisible toast\",\"ingredients\":\"Air\",\"instructions\":\"Imagine it\"}";

            mockMvc.perform(patch("/api/recipes/9999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(patchJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").doesNotExist());

        }

        @Test
        void testDeleteNonExistingRecipe() throws Exception {
            // Skeleton: Try to delete a recipe with a non-existing ID
            // Example: mockMvc.perform(delete("/api/recipes/9999"))...
            //throw new UnsupportedOperationException("testDeleteNonExistingRecipe not implemented");
            mockMvc.perform(delete("/api/recipes/9999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(false));

        }
    }



}