import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import route.user.Friend;
import route.user.User;
import route.user.UserController;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerTest {

    private MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new UserController()).build();

    private User userOne;
    private User userTwo;

    @BeforeAll
    public static void initialize() {
        database.Instance.INSTANCE.setMongo(new MongoClient("localhost").getDatabase("appNameTest"));
    }

    private Map<String, Object> toMap(ResultActions result) throws Exception {
        return new ObjectMapper().readValue(result.andReturn().getResponse().getContentAsString(), HashMap.class);
    }

    private List<Object> toList(ResultActions result) throws Exception {
        return new ObjectMapper().readValue(result.andReturn().getResponse().getContentAsString(), ArrayList.class);
    }

    private boolean resEquals(ResultActions result, Object obj) throws Exception {
        return obj.equals(toMap(result));
    }

    @BeforeEach
    public void reset() {
        database.Instance.INSTANCE.resetMongo();
        userOne = User.Companion.create("4321", "google", "Quinn");
        userTwo = User.Companion.create("1234", "google", "Charlie");
    }

    @Test
    public void getUserById() throws Exception {
        MockHttpServletRequestBuilder getReq;
        ResultActions result;

        getReq = get("/user/" + userOne.getId());
        result = mockMvc.perform(getReq).andExpect(status().isOk());
        assert resEquals(result, userOne);

        getReq = get("/user/" + userOne.getId() + "asdf");
        mockMvc.perform(getReq).andExpect(status().isNotFound());
    }

    @Test
    public void getUserGeneric() throws Exception {
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

        getReq = get("/user").param("oAuthId", userTwo.getOAuthId());
        mockMvc.perform(getReq).andExpect(status().isBadRequest());

        getReq = get("/user").param("oAuthProvider", userTwo.getOAuthProvider());
        mockMvc.perform(getReq).andExpect(status().isBadRequest());

        getReq = get("/user").param("oAuthId", userTwo.getOAuthId()).param("oAuthProvider", userTwo.getOAuthProvider()).param("id", userTwo.getId());
        mockMvc.perform(getReq).andExpect(status().isBadRequest());

        getReq = get("/user").param("id", "thisisafakeid");
        mockMvc.perform(getReq).andExpect(status().isNotFound());

        getReq = get("/user").param("oAuthId", userTwo.getOAuthId()).param("oAuthProvider", "fakeoauthprovider");
        mockMvc.perform(getReq).andExpect(status().isNotFound());
    }

    @Test
    public void putUser() throws Exception {
        MockHttpServletRequestBuilder putReq;
        ResultActions result;
        Document userDoc;

        userDoc = new Document()
                .append("name", "Hulk Hogan")
                .append("oAuthId", "123456")
                .append("oAuthProvider", "google");
        putReq = put("/user").contentType(MediaType.APPLICATION_JSON).content(userDoc.toJson());
        result = mockMvc.perform(putReq).andExpect(status().isOk());
        User createdUser = User.Companion.get(new ObjectId((String) toMap(result).get("id")));
        resEquals(result, createdUser);

        putReq = put("/user").contentType(MediaType.APPLICATION_JSON).content(userDoc.toJson());
        mockMvc.perform(putReq).andExpect(status().isBadRequest());
    }

    @Test
    public void addFriend() throws Exception {
        MockHttpServletRequestBuilder putReq;

        putReq = put("/user/" + userOne.getId() + "/friends/" + userTwo.getId());
        mockMvc.perform(putReq).andExpect(status().isOk());

        putReq = put("/user/" + userOne.getId() + "/friends/" + userTwo.getId());
        mockMvc.perform(putReq).andExpect(status().isBadRequest());

        putReq = put("/user/" + "thisisafakeid" + "/friends/" + userTwo.getId());
        mockMvc.perform(putReq).andExpect(status().isNotFound());

        putReq = put("/user/" + userOne.getId() + "/friends/" + userOne.getId());
        mockMvc.perform(putReq).andExpect(status().isBadRequest());
    }

    @Test
    public void removeFriend() throws Exception {
        MockHttpServletRequestBuilder deleteReq;

        Friend.INSTANCE.addFriend(userOne, userTwo);
        deleteReq = delete("/user/" + userOne.getId() + "/friends/" + userTwo.getId());
        mockMvc.perform(deleteReq).andExpect(status().isOk());

        deleteReq = delete("/user/" + userOne.getId() + "/friends/" + userOne.getId());
        mockMvc.perform(deleteReq).andExpect(status().isBadRequest());

        deleteReq = delete("/user/" + "thisisafakeid" + "/friends/" + userTwo.getId());
        mockMvc.perform(deleteReq).andExpect(status().isNotFound());
    }

    @Test
    public void getFriends() throws Exception {
        MockHttpServletRequestBuilder getReq;
        ResultActions result;

        getReq = get("/user/" + userOne.getId() + "/friends");
        result = mockMvc.perform(getReq).andExpect(status().isOk());
        assert toList(result).size() == 0;

        getReq = get("/user/" + userTwo.getId() + "/friends");
        result = mockMvc.perform(getReq).andExpect(status().isOk());
        assert toList(result).size() == 0;

        Friend.INSTANCE.addFriend(userOne, userTwo);

        getReq = get("/user/" + userOne.getId() + "/friends");
        result = mockMvc.perform(getReq).andExpect(status().isOk());
        assert toList(result).size() == 0;

        getReq = get("/user/" + userTwo.getId() + "/friends");
        result = mockMvc.perform(getReq).andExpect(status().isOk());
        assert toList(result).size() == 0;

        Friend.INSTANCE.addFriend(userTwo, userOne);

        getReq = get("/user/" + userOne.getId() + "/friends");
        result = mockMvc.perform(getReq).andExpect(status().isOk());
        assert toList(result).size() == 1;
        assert userTwo.equals(toList(result).get(0));

        getReq = get("/user/" + userTwo.getId() + "/friends");
        result = mockMvc.perform(getReq).andExpect(status().isOk());
        assert toList(result).size() == 1;
        assert userOne.equals(toList(result).get(0));

        Friend.INSTANCE.removeFriend(userOne, userTwo);

        getReq = get("/user/" + userOne.getId() + "/friends");
        result = mockMvc.perform(getReq).andExpect(status().isOk());
        assert toList(result).size() == 0;

        getReq = get("/user/" + userTwo.getId() + "/friends");
        result = mockMvc.perform(getReq).andExpect(status().isOk());
        assert toList(result).size() == 0;


        getReq = get("/user/" + "thisisafakeid" + "/friends");
        mockMvc.perform(getReq).andExpect(status().isNotFound());
    }

    @Test
    public void getFriendRequests() throws Exception {
        MockHttpServletRequestBuilder getReq;
        ResultActions result;

        getReq = get("/user/" + userOne.getId() + "/friends/requests/sent");
        result = mockMvc.perform(getReq).andExpect(status().isOk());
        assert toList(result).size() == 0;
        getReq = get("/user/" + userOne.getId() + "/friends/requests/received");
        result = mockMvc.perform(getReq).andExpect(status().isOk());
        assert toList(result).size() == 0;

        getReq = get("/user/" + userTwo.getId() + "/friends/requests/sent");
        result = mockMvc.perform(getReq).andExpect(status().isOk());
        assert toList(result).size() == 0;
        getReq = get("/user/" + userTwo.getId() + "/friends/requests/received");
        result = mockMvc.perform(getReq).andExpect(status().isOk());
        assert toList(result).size() == 0;

        Friend.INSTANCE.addFriend(userOne, userTwo);

        getReq = get("/user/" + userOne.getId() + "/friends/requests/sent");
        result = mockMvc.perform(getReq).andExpect(status().isOk());
        assert toList(result).size() == 1;
        assert userTwo.equals(toList(result).get(0));
        getReq = get("/user/" + userOne.getId() + "/friends/requests/received");
        result = mockMvc.perform(getReq).andExpect(status().isOk());
        assert toList(result).size() == 0;

        getReq = get("/user/" + userTwo.getId() + "/friends/requests/sent");
        result = mockMvc.perform(getReq).andExpect(status().isOk());
        assert toList(result).size() == 0;
        getReq = get("/user/" + userTwo.getId() + "/friends/requests/received");
        result = mockMvc.perform(getReq).andExpect(status().isOk());
        assert toList(result).size() == 1;
        assert userOne.equals(toList(result).get(0));

        Friend.INSTANCE.addFriend(userTwo, userOne);

        getReq = get("/user/" + userOne.getId() + "/friends/requests/sent");
        result = mockMvc.perform(getReq).andExpect(status().isOk());
        assert toList(result).size() == 0;
        getReq = get("/user/" + userOne.getId() + "/friends/requests/received");
        result = mockMvc.perform(getReq).andExpect(status().isOk());
        assert toList(result).size() == 0;

        getReq = get("/user/" + userTwo.getId() + "/friends/requests/sent");
        result = mockMvc.perform(getReq).andExpect(status().isOk());
        assert toList(result).size() == 0;
        getReq = get("/user/" + userTwo.getId() + "/friends/requests/received");
        result = mockMvc.perform(getReq).andExpect(status().isOk());
        assert toList(result).size() == 0;


        getReq = get("/user/" + "thisisafakeid" + "/friends/requests/sent");
        mockMvc.perform(getReq).andExpect(status().isNotFound());
        getReq = get("/user/" + "thisisafakeid" + "/friends/requests/received");
        mockMvc.perform(getReq).andExpect(status().isNotFound());
    }
}