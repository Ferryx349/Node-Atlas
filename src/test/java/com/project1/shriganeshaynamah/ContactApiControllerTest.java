package com.project1.shriganeshaynamah;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.project1.shriganeshaynamah.Dao.contacc;
import com.project1.shriganeshaynamah.Dao.userDao;
import com.project1.shriganeshaynamah.user.Contact;
import com.project1.shriganeshaynamah.user.User;

@SpringBootTest
@AutoConfigureMockMvc
class ContactApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private userDao userRepository;

    @Autowired
    private contacc contactRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        contactRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setName("apiuser");
        user.setEmail("api@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole("USER");
        user.setEnable("true");
        userRepository.save(user);
    }

    @Test
    void createAndListContactsViaApi() throws Exception {
        String body = """
            {"name":"Jane Doe","email":"jane@example.com","phone":"9999999999","category":"Work","favorite":true}
            """;

        mockMvc.perform(post("/api/contacts")
                .with(csrf())
                .with(user("apiuser"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("Jane Doe"));

        mockMvc.perform(get("/api/contacts")
                .with(user("apiuser")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalItems").value(1));
    }

    @Test
    void getStatsViaApi() throws Exception {
        User user = userRepository.findByName("apiuser");
        Contact contact = new Contact();
        contact.setName("Stat Test");
        contact.setCategory("Work");
        contact.setFavorite(true);
        contact.setUs(user);
        contactRepository.save(contact);

        mockMvc.perform(get("/api/contacts/stats")
                .with(user("apiuser")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalContacts").value(1))
            .andExpect(jsonPath("$.data.favoriteContacts").value(1));
    }

    @Test
    void uploadContactImageViaApi() throws Exception {
        User user = userRepository.findByName("apiuser");
        Contact contact = new Contact();
        contact.setName("Image Test");
        contact.setUs(user);
        contact = contactRepository.save(contact);

        byte[] imageBytes = new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
        };
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", imageBytes);

        mockMvc.perform(multipart("/api/contacts/" + contact.getCid() + "/image")
                .file(file)
                .with(csrf())
                .with(user("apiuser")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.imageUrl").isNotEmpty());
    }

    @Test
    void updateAndDeleteContactViaApi() throws Exception {
        User user = userRepository.findByName("apiuser");
        Contact contact = new Contact();
        contact.setName("Old Name");
        contact.setUs(user);
        contact = contactRepository.save(contact);

        String updateBody = """
            {"name":"New Name","email":"new@example.com","category":"Personal","favorite":false}
            """;

        mockMvc.perform(put("/api/contacts/" + contact.getCid())
                .with(csrf())
                .with(user("apiuser"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("New Name"));

        mockMvc.perform(delete("/api/contacts/" + contact.getCid())
                .with(csrf())
                .with(user("apiuser")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}
