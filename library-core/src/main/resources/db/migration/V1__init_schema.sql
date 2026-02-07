/**
 * DATABASE MIGRATION: V1__Initial_Schema
 * Description: Sets up the core Library tables with automated auditing triggers
 * and performance-optimized indexes.
 */

-- 1. AUDIT TRIGGER FUNCTION
-- This function automatically refreshes the 'updated_at' timestamp on any row change.
-- It ensures data integrity by preventing the application from accidentally
-- leaving old timestamps during an UPDATE operation.
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 2. CORE TABLES

-- BOOKS: Inventory tracking
CREATE TABLE books (
    id UUID PRIMARY KEY,
    book_number BIGINT UNIQUE NOT NULL,
    isbn VARCHAR(255) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    total_copies INTEGER NOT NULL DEFAULT 0,
    available_copies INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Handled by Postgres on INSERT
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Handled by Trigger on UPDATE
    deleted BOOLEAN NOT NULL DEFAULT FALSE -- Soft-delete support
);

-- MEMBERS: User management
CREATE TABLE members (
    id UUID PRIMARY KEY,
    membership_number BIGINT UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- LOANS: The junction table linking Members and Books
CREATE TABLE loans (
    id UUID PRIMARY KEY,
    book_id UUID NOT NULL REFERENCES books(id),
    member_id UUID NOT NULL REFERENCES members(id),
    borrowed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    due_date DATE NOT NULL, -- Date only (no time) as library policies are usually calendar-day based
    returned_at TIMESTAMP WITH TIME ZONE, -- NULL indicates an active loan
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- 3. TRIGGER ATTACHMENTS
-- Before any UPDATE reaches the disk, these triggers fire the function defined in Step 1.
CREATE TRIGGER update_books_modtime BEFORE UPDATE ON books FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_members_modtime BEFORE UPDATE ON members FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_loans_modtime BEFORE UPDATE ON loans FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- 4. Create Indexes

-- BOOKS Table Indexes
-- Unique index for ISBN (used for book identification)
CREATE UNIQUE INDEX idx_book_isbn ON books(isbn);
-- Unique index for internal library tracking number
CREATE UNIQUE INDEX idx_book_number ON books(book_number);
-- Standard B-tree indexes for search functionality
CREATE INDEX idx_book_title ON books(title);
CREATE INDEX idx_book_author ON books(author);

-- MEMBERS Table Indexes
-- Unique index for login/contact email
CREATE UNIQUE INDEX idx_member_email ON members(email);
-- Unique index for physical membership card scanning
CREATE UNIQUE INDEX idx_member_membership_number ON members(membership_number);
-- Standard index for directory searching by surname
CREATE INDEX idx_member_last_name ON members(last_name);

-- LOANS Table Indexes
-- Composite Index: Optimized for finding unreturned books for a specific member
CREATE INDEX idx_active_loans_member ON loans(member_id, returned_at);
-- Optimized for overdue loan reports and background batch processing
CREATE INDEX idx_loan_due_date ON loans(due_date);
-- Auditing index for timeline sorting and dashboard metrics
CREATE INDEX idx_loan_created_at ON loans(created_at);