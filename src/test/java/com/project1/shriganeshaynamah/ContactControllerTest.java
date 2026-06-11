package com.project1.shriganeshaynamah;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.project1.shriganeshaynamah.Dao.contacc;
import com.project1.shriganeshaynamah.Dao.userDao;
import com.project1.shriganeshaynamah.user.Contact;
import com.project1.shriganeshaynamah.user.User;

@SpringBootTest
@AutoConfigureMockMvc
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private userDao userRepository;

    @Autowired
    private contacc contactRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        contactRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole("USER");
        testUser.setEnable("true");
        testUser = userRepository.save(testUser);
    }

    @Test
    void homePageIsPublic() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("home"));
    }

    @Test
    void userDashboardRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/user/index"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void authenticatedUserCanAccessDashboard() throws Exception {
        mockMvc.perform(get("/user/index").with(user("testuser")))
            .andExpect(status().isOk())
            .andExpect(view().name("user/index"));
    }

    @Test
    void userCanAddContact() throws Exception {
        mockMvc.perform(post("/user/a-contact")
                .with(csrf())
                .with(user("testuser"))
                .param("name", "John Doe")
                .param("email", "john@example.com")
                .param("phone", "1234567890"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/user/show/0"));
    }

    @Test
    void userCanSearchContacts() throws Exception {
        Contact contact = new Contact();
        contact.setName("Alice Smith");
        contact.setEmail("alice@example.com");
        contact.setUs(testUser);
        contactRepository.save(contact);

        mockMvc.perform(get("/user/show/0").param("keyword", "Alice").with(user("testuser")))
            .andExpect(status().isOk())
            .andExpect(view().name("user/scontact"));
    }
}
