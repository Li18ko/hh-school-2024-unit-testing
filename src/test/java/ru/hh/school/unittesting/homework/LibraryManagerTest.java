package ru.hh.school.unittesting.homework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryManagerTest {

    @Mock
    private UserService userService;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private LibraryManager libraryManager;


    @BeforeEach
    void setUp(){
        libraryManager.addBook("book1", 30);
        libraryManager.addBook("book2", 45);
        libraryManager.addBook("book3", 0);
    }

    @ParameterizedTest
    @CsvSource({
            "book1, 30",
            "book2, 45",
            "book3, 0"
    })
    void testAddBook(String bookId,
                     int expectedQuantity
    ){
        assertEquals(expectedQuantity, libraryManager.getAvailableCopies(bookId));
    }

    @Test
    void borrowBookSuccessfully(){
        when(userService.isUserActive("user1")).thenReturn(true);

        boolean resultBorrowBook = libraryManager.borrowBook("book1", "user1");

        assertTrue(resultBorrowBook);
        assertEquals(29, libraryManager.getAvailableCopies("book1"));
        verify(notificationService).notifyUser("user1", "You have borrowed the book: book1");
    }

    @Test
    void borrowBookNotUserActive(){
        when(userService.isUserActive("user2")).thenReturn(false);

        boolean resultBorrowBook = libraryManager.borrowBook("book2", "user2");

        assertFalse(resultBorrowBook);
        assertEquals(45, libraryManager.getAvailableCopies("book2"));
        verify(notificationService).notifyUser("user2", "Your account is not active.");
    }

    @Test
    void borrowBookNotAvailableCopies(){
        when(userService.isUserActive("user3")).thenReturn(true);

        boolean resultBorrowBook = libraryManager.borrowBook("book3", "user3");


        assertFalse(resultBorrowBook);
        assertEquals(0, libraryManager.getAvailableCopies("book3"));
        verify(notificationService, never()).notifyUser("user3", "You have borrowed the book: book3");
    }

    @Test
    void returnBookSuccessfully(){
        when(userService.isUserActive("user1")).thenReturn(true);
        libraryManager.borrowBook("book1", "user1");

        boolean resultReturnBook = libraryManager.returnBook("book1", "user1");

        assertTrue(resultReturnBook);
        assertEquals(30, libraryManager.getAvailableCopies("book1"));
        verify(notificationService).notifyUser("user1", "You have returned the book: book1");
    }

    @Test
    void returnBookAnotherUser(){
        when(userService.isUserActive("user1")).thenReturn(true);
        libraryManager.borrowBook("book1", "user1");

        boolean resultReturnBook = libraryManager.returnBook("book1", "user5");

        assertFalse(resultReturnBook);
        assertEquals(29, libraryManager.getAvailableCopies("book1"));
        verify(notificationService, never()).notifyUser("user5", "You have returned the book: book1");
    }

    @Test
    void returnBookWhichWasNotTaken(){
        when(userService.isUserActive("user1")).thenReturn(true);
        libraryManager.borrowBook("book2", "user1");

        boolean resultReturnBook = libraryManager.returnBook("book1", "user1");

        assertFalse(resultReturnBook);
        assertEquals(30, libraryManager.getAvailableCopies("book1"));
        verify(notificationService, never()).notifyUser("user1", "You have returned the book: book1");
    }

    @Test
    void calculateDynamicLateFeeDefaultBook(){
        double result = libraryManager.calculateDynamicLateFee(5, false, false);
        assertEquals(2.5, result);
    }

    @Test
    void calculateDynamicLateFeeIsBestseller(){
        double result = libraryManager.calculateDynamicLateFee(5, true, false);
        assertEquals(3.75, result);
    }

    @Test
    void calculateDynamicLateFeeIsPremiumMember(){
        double result = libraryManager.calculateDynamicLateFee(5, false, true);
        assertEquals(2.0, result);
    }

    @Test
    void calculateDynamicLateFeeIsBestsellerAndIsPremiumMember(){
        double result = libraryManager.calculateDynamicLateFee(5, true, true);
        assertEquals(3.0, result);
    }

    @Test
    void calculateDynamicLateFeeThrowExceptioNullDays(){
        double result = libraryManager.calculateDynamicLateFee(0, false, false);
        assertEquals(0, result);
    }

    @Test
    void calculateDynamicLateFeeThrowExceptionNegativeDays(){
        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> libraryManager.calculateDynamicLateFee(-2, true, true)
        );
        assertEquals("Overdue days cannot be negative.", exception.getMessage());
    }

}
