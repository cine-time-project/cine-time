package com.cinetime.service.business;


import com.cinetime.entity.business.ContactMessage;
import com.cinetime.payload.request.business.ContactMessageRequest;
import com.cinetime.repository.business.ContactMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Tek sınıfta birim test: ContactMessageServiceImpl.save()
 * - Repository mock
 * - Alanların doğru map’lenmesi kontrolü
 * - repo.save çağrısı ve dönen değerin geri verilmesi
 * - Hata durumunda davranış (propagate)
 */
@ExtendWith(MockitoExtension.class)
class ContactMessageServiceImplTest {

    @Mock
    private ContactMessageRepository repo;

    private ContactMessageServiceImpl service;

    @Captor
    private ArgumentCaptor<ContactMessage> messageCaptor;

    @BeforeEach
    void setUp() {
        service = new ContactMessageServiceImpl(repo);
    }

    private static ContactMessageRequest sampleRequest() {
        ContactMessageRequest req = new ContactMessageRequest();
        // Varsayılan getter/setter isimlerine göre:
        req.setFullName("Esra Yılmaz");
        req.setEmail("esra@example.com");
        req.setPhoneNumber("(312) 813-2564");
        req.setSubject("Bilet iadesi");
        req.setMessage("Seansı değiştirmek istiyorum.");
        return req;
    }

    @Test
    @DisplayName("save(): Request alanları ContactMessage'a doğru map'lenmeli ve repo.save çağrılmalı")
    void save_shouldMapFields_andCallRepositorySave() {
        // given
        ContactMessageRequest req = sampleRequest();

        // repo.save dönerken id set'lenmiş bir entity dönsün (DB davranışını taklit ediyoruz)
        ContactMessage persisted = new ContactMessage();
        persisted.setId(42L);
        persisted.setFullName(req.getFullName());
        persisted.setEmail(req.getEmail());
        persisted.setPhoneNumber(req.getPhoneNumber());
        persisted.setSubject(req.getSubject());
        persisted.setMessage(req.getMessage());

        when(repo.save(any(ContactMessage.class))).thenReturn(persisted);

        // when
        ContactMessage result = service.save(req);

        // then: repo.save doğru argümanla çağrıldı mı?
        verify(repo, times(1)).save(messageCaptor.capture());
        ContactMessage captured = messageCaptor.getValue();

        assertThat(captured.getId()).as("Yeni kayıt öncesi id set edilmemeli").isNull();
        assertThat(captured.getFullName()).isEqualTo(req.getFullName());
        assertThat(captured.getEmail()).isEqualTo(req.getEmail());
        assertThat(captured.getPhoneNumber()).isEqualTo(req.getPhoneNumber());
        assertThat(captured.getSubject()).isEqualTo(req.getSubject());
        assertThat(captured.getMessage()).isEqualTo(req.getMessage());

        // ve servis repo.save'in döndürdüğünü aynen dönmeli
        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getFullName()).isEqualTo(req.getFullName());
        assertThat(result.getEmail()).isEqualTo(req.getEmail());
        assertThat(result.getPhoneNumber()).isEqualTo(req.getPhoneNumber());
        assertThat(result.getSubject()).isEqualTo(req.getSubject());
        assertThat(result.getMessage()).isEqualTo(req.getMessage());
    }

    @Test
    @DisplayName("save(): Repository hata fırlatırsa servis de propagate etmeli")
    void save_shouldPropagateRepositoryException() {
        // given
        ContactMessageRequest req = sampleRequest();
        when(repo.save(any(ContactMessage.class)))
                .thenThrow(new RuntimeException("DB down"));

        // when/then
        assertThatThrownBy(() -> service.save(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB down");

        verify(repo, times(1)).save(any(ContactMessage.class));
    }
}
