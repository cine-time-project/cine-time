package com.cinetime.service.mail;


import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.*;

class MailServiceTest {

    @Mock private JavaMailSender mailSender;
    @InjectMocks private MailService mailService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendResetCode_ShouldSendMail() throws Exception {
        MimeMessage mime = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mime);

        // test
        mailService.sendResetCode("user@example.com", "123456");

        // verify that send is called
        verify(mailSender).send(any(MimeMessage.class));
    }
}
