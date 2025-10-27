package com.cinetime.service.business;

import com.cinetime.entity.business.ContactMessage;
import com.cinetime.payload.request.business.ContactMessageRequest;
import com.cinetime.repository.business.ContactMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactMessageServiceImplTest {

    @Mock
    private ContactMessageRepository repo;

    @InjectMocks
    private ContactMessageServiceImpl service;

    @Captor
    private ArgumentCaptor<ContactMessage> cmCaptor;

    private ContactMessageRequest req;

    @BeforeEach
    void setUp() {
        req = new ContactMessageRequest();
        req.setFullName("Ada Lovelace");
        req.setEmail("ada@cinetime.example");
        req.setPhoneNumber("(555) 123-4567");
        req.setSubject("Hello");
        req.setMessage("Merhaba, bilet iadesi hakkında bilgi alabilir miyim?");
    }

    @Test
    @DisplayName("save(): Request alanları entity'e doğru map edilir ve repo.save çağrılır")
    void save_mapsAndPersists() {
        when(repo.save(any(ContactMessage.class))).thenAnswer(inv -> {
            ContactMessage arg = inv.getArgument(0);
            // Yeni bir entity dön: arg'ı MUTASYONLAMA
            ContactMessage saved = new ContactMessage();
            saved.setId(42L);
            saved.setFullName(arg.getFullName());
            saved.setEmail(arg.getEmail());
            saved.setPhoneNumber(arg.getPhoneNumber());
            saved.setSubject(arg.getSubject());
            saved.setMessage(arg.getMessage());
            return saved;
        });

        ContactMessage result = service.save(req);

        verify(repo).save(cmCaptor.capture());
        ContactMessage captured = cmCaptor.getValue();

        // repo.save'e giden nesnede id set edilmemeli
        assertThat(captured.getId()).isNull();
        assertThat(captured).usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(copyFromReq(req));

        // repo.save dönüşü id almış olmalı
        assertThat(result.getId()).isEqualTo(42L);
    }

    // küçük yardımcı
    private static ContactMessage copyFromReq(ContactMessageRequest r) {
        ContactMessage cm = new ContactMessage();
        cm.setFullName(r.getFullName());
        cm.setEmail(r.getEmail());
        cm.setPhoneNumber(r.getPhoneNumber());
        cm.setSubject(r.getSubject());
        cm.setMessage(r.getMessage());
        return cm;
    }

    @Test
    @DisplayName("getAll(): repo.findAll sonucu aynen döner")
    void getAll_returnsList() {
        List<ContactMessage> data = Arrays.asList(
                makeCM(1L, "A", "a@x.com"),
                makeCM(2L, "B", "b@x.com")
        );
        when(repo.findAll()).thenReturn(data);

        List<ContactMessage> result = service.getAll();

        verify(repo, times(1)).findAll();
        assertThat(result).hasSize(2).containsExactlyElementsOf(data);
    }

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("Kayıt bulunduğunda nesneyi döndürür")
        void found_returnsEntity() {
            ContactMessage cm = makeCM(10L, "C", "c@x.com");
            when(repo.findById(10L)).thenReturn(Optional.of(cm));

            ContactMessage result = service.getById(10L);

            verify(repo, times(1)).findById(10L);
            assertThat(result).isSameAs(cm);
        }

        @Test
        @DisplayName("Kayıt bulunamadığında RuntimeException fırlatır")
        void notFound_throws() {
            when(repo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getById(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Contact message not found with id: 99");

            verify(repo, times(1)).findById(99L);
        }
    }

    @Nested
    @DisplayName("deleteById()")
    class DeleteById {

        @Test
        @DisplayName("Kayıt varsa siler ve repo.deleteById çağrılır")
        void existing_deletes() {
            when(repo.existsById(7L)).thenReturn(true);

            service.deleteById(7L);

            verify(repo, times(1)).existsById(7L);
            verify(repo, times(1)).deleteById(7L);
        }

        @Test
        @DisplayName("Kayıt yoksa RuntimeException fırlatır ve deleteById çağrılmaz")
        void missing_throws() {
            when(repo.existsById(8L)).thenReturn(false);

            assertThatThrownBy(() -> service.deleteById(8L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Contact message not found with id: 8");

            verify(repo, times(1)).existsById(8L);
            verify(repo, never()).deleteById(anyLong());
        }
    }

    // ---- helper ----
    private static ContactMessage makeCM(Long id, String fullName, String email) {
        ContactMessage cm = new ContactMessage();
        cm.setId(id);
        cm.setFullName(fullName);
        cm.setEmail(email);
        cm.setPhoneNumber("(000) 000-0000");
        cm.setSubject("S");
        cm.setMessage("M");
        return cm;
    }
}
