import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import route.user.User;
import route.user.UserController;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerTest {

    private MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new UserController()).build();

    private User userOne;
    private User userTwo;

    @BeforeAll
    public static void initialize() {
        database.Instance.INSTANCE.setMongo(new MongoClient("localhost").getDatabase("appNameTest"));
    }

    private boolean resEquals(ResultActions result, Object obj) throws Exception {
        Map<String, Object> map = new ObjectMapper().readValue(result.andReturn().getResponse().getContentAsString(), HashMap.class);
        return obj.equals(map);
    }

    @BeforeEach
    public void reset() {
        database.Instance.INSTANCE.resetMongo();
        userOne = User.Companion.create("4321", "google", "Quinn");
        userTwo = User.Companion.create("1234", "google", "Charlie");
    }

    @Test
    public void getById() throws Exception {
        MockHttpServletRequestBuilder getReq = get("/user/" + userOne.getId());
        ResultActions result = mockMvc.perform(getReq);
        result.andExpect(status().isOk());
        assert resEquals(result, userOne);
    }

    @Test
    public void getGeneric() throws Exception {
        MockHttpServletRequestBuilder getReq;
        ResultActions result;

        getReq = get("/user").param("id", userTwo.getId());
        result = mockMvc.perform(getReq).andExpect(status().isOk());
        assert resEquals(result, userTwo);

        getReq = get("/user").param("oAuthId", userTwo.getOAuthId()).param("oAuthProvider", userTwo.getOAuthProvider());
        result = mockMvc.perform(getReq).andExpect(status().isOk());
        assert resEquals(result, userTwo);

        getReq = get("/user");
        mockMvc.perform(getReq).andExpect(status().isBadRequest());
    }
}