package org.service.mock;

import org.data.Cashier;
import org.data.Product;
import org.data.ProductCategory;
import org.data.Receipt;
import org.data.Store;
import org.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.service.CashierService;
import org.service.ProductService;
import org.service.ReceiptService;
import org.service.PricingService;
import org.service.impl.StoreServiceImpl;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class StoreServiceMockTest {
    @Mock
    private ProductService productService;
    @Mock
    private CashierService cashierService;
    @Mock
    private ReceiptService receiptService;
    @Mock
    private PricingService pricingService;
    @Mock
    private Store store;
    
    @InjectMocks
    private StoreServiceImpl storeService;
    
    private Product mockProduct;
    private Cashier mockCashier;
    private Receipt mockReceipt;
    
    @BeforeEach
    void setUp() {
        mockProduct = mock(Product.class);
        mockCashier = mock(Cashier.class);
        mockReceipt = mock(Receipt.class);
        reset(productService, cashierService, receiptService, pricingService, store, mockProduct, mockCashier, mockReceipt);
    }
    
    @Test
    void testCreateSaleWithMocks() throws InsufficientQuantityException {
        // Arrange
        when(mockProduct.getId()).thenReturn(1);
        when(mockProduct.getCategory()).thenReturn(ProductCategory.FOOD);
        when(mockProduct.getQuantity()).thenReturn(10);
        when(productService.getProduct(1)).thenReturn(mockProduct);
        when(cashierService.getCashierAtRegister(1)).thenReturn(mockCashier);
        when(pricingService.calculateSellingPrice(1, 0.2)).thenReturn(12.0);
        when(receiptService.createReceipt(any(), anyInt(), any(), anyDouble()))
            .thenReturn(mockReceipt);
            
        // Act
        Map<Integer, Integer> purchase = new HashMap<>();
        purchase.put(1, 2);
        Receipt receipt = storeService.createSale(1, purchase);
        
        // Assert
        assertNotNull(receipt);
        verify(productService).updateProductQuantity(1, 8);
        verify(receiptService).createReceipt(eq(mockCashier), eq(1), any(), eq(24.0));
        verify(pricingService).calculateSellingPrice(1, 0.2);
    }
    
    @Test
    void testCreateSaleWithInsufficientQuantity() {
        // Arrange
        when(mockProduct.getId()).thenReturn(1);
        when(mockProduct.getQuantity()).thenReturn(5);
        when(productService.getProduct(1)).thenReturn(mockProduct);
        when(cashierService.getCashierAtRegister(1)).thenReturn(mockCashier);
        
        // Act & Assert
        Map<Integer, Integer> purchase = new HashMap<>();
        purchase.put(1, 10);
        assertThrows(InsufficientQuantityException.class, () -> {
            storeService.createSale(1, purchase);
        });
    }
    
    @Test
    void testCreateSaleWithExpiredProduct() {
        // Arrange
        when(mockProduct.getId()).thenReturn(1);
        when(productService.getProduct(1)).thenReturn(mockProduct);
        when(cashierService.getCashierAtRegister(1)).thenReturn(mockCashier);
        when(pricingService.isProductExpired(1)).thenReturn(true);
        
        // Act & Assert
        Map<Integer, Integer> purchase = new HashMap<>();
        purchase.put(1, 1);
        assertThrows(ExpiredProductException.class, () -> {
            storeService.createSale(1, purchase);
        });
    }
    
    @Test
    void testCreateSaleWithNoAssignedCashier() {
        // Arrange
        when(cashierService.getCashierAtRegister(1)).thenReturn(null);
        
        // Act & Assert
        Map<Integer, Integer> purchase = new HashMap<>();
        purchase.put(1, 1);
        assertThrows(NoAssignedCashierException.class, () -> {
            storeService.createSale(1, purchase);
        });
    }
    
    @Test
    void testAssignCashierToRegister() {
        // Arrange
        when(cashierService.getCashierAtRegister(1)).thenReturn(null);
        
        // Act
        storeService.assignCashierToRegister(mockCashier, 1);
        
        // Assert
        verify(cashierService).assignCashierToRegister(mockCashier.getId(), 1);
    }
    
    @Test
    void testAssignCashierToAlreadyAssignedRegister() {
        // Arrange
        when(cashierService.getCashierAtRegister(1)).thenReturn(mock(Cashier.class));
        
        // Act & Assert
        assertThrows(RegisterAlreadyAssignedException.class, () -> {
            storeService.assignCashierToRegister(mockCashier, 1);
        });
    }
    
    @Test
    void testGetFinancialMetrics() {
        // Arrange
        when(receiptService.getTotalRevenue()).thenReturn(1000.0);
        when(cashierService.getTotalSalaryExpenses()).thenReturn(500.0);
        when(pricingService.getTotalDeliveryExpenses()).thenReturn(300.0);
        
        // Act
        double revenue = storeService.getTotalRevenue();
        double salaryExpenses = storeService.getSalaryExpenses();
        double deliveryExpenses = storeService.getDeliveryExpenses();
        double income = storeService.getIncome();
        double profit = storeService.getProfit();
        
        // Assert
        assertEquals(1000.0, revenue);
        assertEquals(500.0, salaryExpenses);
        assertEquals(300.0, deliveryExpenses);
        assertEquals(700.0, income);
        assertEquals(200.0, profit);
    }
} 