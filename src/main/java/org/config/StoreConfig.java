package org.config;

import java.io.Serializable;

/**
 * Configuration class for store operations, particularly focused on error handling
 * and file operations. This class centralizes configuration parameters that were
 * previously hardcoded in various service implementations.
 */
public class StoreConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // File operation configurations
    private final String receiptOutputDir;
    private final int maxRetryAttempts;
    private final long retryDelayMs;
    
    // Error handling configurations
    private final boolean throwExceptionOnDirectoryCreationFailure;
    private final boolean createMissingDirectories;
    
    /**
     * Default constructor with sensible defaults
     */
    public StoreConfig() {
        this("output/receipts", 3, 1000, true, true);
    }
    
    /**
     * Full constructor allowing all parameters to be specified
     */
    public StoreConfig(String receiptOutputDir, int maxRetryAttempts, long retryDelayMs,
                      boolean throwExceptionOnDirectoryCreationFailure, boolean createMissingDirectories) {
        this.receiptOutputDir = receiptOutputDir;
        this.maxRetryAttempts = maxRetryAttempts;
        this.retryDelayMs = retryDelayMs;
        this.throwExceptionOnDirectoryCreationFailure = throwExceptionOnDirectoryCreationFailure;
        this.createMissingDirectories = createMissingDirectories;
    }
    
    /**
     * Builder pattern for more flexible configuration
     */
    public static class Builder {
        private String receiptOutputDir = "output/receipts";
        private int maxRetryAttempts = 3;
        private long retryDelayMs = 1000;
        private boolean throwExceptionOnDirectoryCreationFailure = true;
        private boolean createMissingDirectories = true;
        
        public Builder receiptOutputDir(String receiptOutputDir) {
            this.receiptOutputDir = receiptOutputDir;
            return this;
        }
        
        public Builder maxRetryAttempts(int maxRetryAttempts) {
            this.maxRetryAttempts = maxRetryAttempts;
            return this;
        }
        
        public Builder retryDelayMs(long retryDelayMs) {
            this.retryDelayMs = retryDelayMs;
            return this;
        }
        
        public Builder throwExceptionOnDirectoryCreationFailure(boolean throwExceptionOnDirectoryCreationFailure) {
            this.throwExceptionOnDirectoryCreationFailure = throwExceptionOnDirectoryCreationFailure;
            return this;
        }
        
        public Builder createMissingDirectories(boolean createMissingDirectories) {
            this.createMissingDirectories = createMissingDirectories;
            return this;
        }
        
        public StoreConfig build() {
            return new StoreConfig(receiptOutputDir, maxRetryAttempts, retryDelayMs,
                    throwExceptionOnDirectoryCreationFailure, createMissingDirectories);
        }
    }
    
    // Getters
    
    public String getReceiptOutputDir() {
        return receiptOutputDir;
    }
    
    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }
    
    public long getRetryDelayMs() {
        return retryDelayMs;
    }
    
    public boolean isThrowExceptionOnDirectoryCreationFailure() {
        return throwExceptionOnDirectoryCreationFailure;
    }
    
    public boolean isCreateMissingDirectories() {
        return createMissingDirectories;
    }
}