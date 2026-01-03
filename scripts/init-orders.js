// Order Service Database Initialization Script
// Creates sample order data in MongoDB for testing and demonstration

// Use the bookstore_orders database
db = db.getSiblingDB('bookstore_orders');

// Clear existing data (for fresh initialization)
db.orders.deleteMany({});

print('Initializing orders database with sample data...');

// Insert 10 sample orders with varied statuses and realistic data
// Note: userId values correspond to users in the PostgreSQL database
// Note: bookId values correspond to books in the MySQL catalog database

const sampleOrders = [
    {
        userId: 1, // admin@bookstore.com
        items: [
            { bookId: 1, isbn: '9780134685991', title: 'Effective Java', quantity: 2, price: 45.99 },
            { bookId: 4, isbn: '9780132350884', title: 'Clean Code', quantity: 1, price: 44.95 }
        ],
        totalAmount: 136.93,
        status: 'DELIVERED',
        shippingAddress: '123 Admin Street, Tech City, TC 12345',
        orderDate: new Date('2024-12-15T10:30:00Z'),
        deliveryDate: new Date('2024-12-20T14:00:00Z')
    },
    {
        userId: 2, // john.doe@bookstore.com
        items: [
            { bookId: 6, isbn: '9780061120084', title: 'To Kill a Mockingbird', quantity: 1, price: 18.99 },
            { bookId: 7, isbn: '9780451524935', title: '1984', quantity: 1, price: 16.99 },
            { bookId: 9, isbn: '9780743273565', title: 'The Great Gatsby', quantity: 1, price: 15.99 }
        ],
        totalAmount: 51.97,
        status: 'DELIVERED',
        shippingAddress: '456 Oak Avenue, Reading Town, RT 67890',
        orderDate: new Date('2024-12-18T14:20:00Z'),
        deliveryDate: new Date('2024-12-23T16:30:00Z')
    },
    {
        userId: 2, // john.doe@bookstore.com
        items: [
            { bookId: 3, isbn: '9781617294945', title: 'Spring Boot in Action', quantity: 1, price: 42.50 }
        ],
        totalAmount: 42.50,
        status: 'SHIPPED',
        shippingAddress: '456 Oak Avenue, Reading Town, RT 67890',
        orderDate: new Date('2025-01-01T09:15:00Z'),
        shippedDate: new Date('2025-01-02T11:00:00Z')
    },
    {
        userId: 3, // jane.smith@bookstore.com
        items: [
            { bookId: 13, isbn: '9781591847816', title: 'Atomic Habits', quantity: 2, price: 27.99 },
            { bookId: 14, isbn: '9780062316097', title: 'Sapiens', quantity: 1, price: 24.99 }
        ],
        totalAmount: 80.97,
        status: 'CONFIRMED',
        shippingAddress: '789 Maple Drive, Book City, BC 11223',
        orderDate: new Date('2025-01-02T16:45:00Z'),
        confirmedDate: new Date('2025-01-02T17:00:00Z')
    },
    {
        userId: 4, // mike.wilson@bookstore.com
        items: [
            { bookId: 11, isbn: '9780547928227', title: 'The Hobbit', quantity: 1, price: 19.99 },
            { bookId: 12, isbn: '9780441013593', title: 'Dune', quantity: 1, price: 21.99 }
        ],
        totalAmount: 41.98,
        status: 'SHIPPED',
        shippingAddress: '321 Pine Street, Fantasy Land, FL 33445',
        orderDate: new Date('2024-12-28T13:30:00Z'),
        shippedDate: new Date('2024-12-30T10:15:00Z')
    },
    {
        userId: 5, // sarah.jones@bookstore.com
        items: [
            { bookId: 16, isbn: '9780307949486', title: 'The Girl with the Dragon Tattoo', quantity: 1, price: 19.99 },
            { bookId: 17, isbn: '9780062073488', title: 'Gone Girl', quantity: 1, price: 18.99 }
        ],
        totalAmount: 38.98,
        status: 'DELIVERED',
        shippingAddress: '654 Cedar Lane, Mystery Town, MT 55667',
        orderDate: new Date('2024-12-10T11:00:00Z'),
        deliveryDate: new Date('2024-12-16T15:30:00Z')
    },
    {
        userId: 6, // alex.brown@bookstore.com
        items: [
            { bookId: 5, isbn: '9781449373320', title: 'Designing Data-Intensive Applications', quantity: 1, price: 59.99 }
        ],
        totalAmount: 59.99,
        status: 'PENDING',
        shippingAddress: '987 Birch Road, Developer City, DC 77889',
        orderDate: new Date('2025-01-03T08:00:00Z')
    },
    {
        userId: 3, // jane.smith@bookstore.com
        items: [
            { bookId: 18, isbn: '9780393347777', title: 'Educated', quantity: 1, price: 22.99 },
            { bookId: 19, isbn: '9780385353755', title: 'Becoming', quantity: 1, price: 26.99 }
        ],
        totalAmount: 49.98,
        status: 'DELIVERED',
        shippingAddress: '789 Maple Drive, Book City, BC 11223',
        orderDate: new Date('2024-12-05T10:30:00Z'),
        deliveryDate: new Date('2024-12-12T14:00:00Z')
    },
    {
        userId: 4, // mike.wilson@bookstore.com
        items: [
            { bookId: 10, isbn: '9780345391803', title: 'The Hitchhiker\'s Guide to the Galaxy', quantity: 2, price: 17.99 }
        ],
        totalAmount: 35.98,
        status: 'CANCELLED',
        shippingAddress: '321 Pine Street, Fantasy Land, FL 33445',
        orderDate: new Date('2024-12-20T15:00:00Z'),
        cancelledDate: new Date('2024-12-21T09:00:00Z'),
        cancellationReason: 'Customer requested cancellation'
    },
    {
        userId: 6, // alex.brown@bookstore.com
        items: [
            { bookId: 15, isbn: '9781501124020', title: 'The Lean Startup', quantity: 1, price: 29.99 },
            { bookId: 20, isbn: '9780143127796', title: 'Me Before You', quantity: 1, price: 16.99 }
        ],
        totalAmount: 46.98,
        status: 'CONFIRMED',
        shippingAddress: '987 Birch Road, Developer City, DC 77889',
        orderDate: new Date('2025-01-02T12:00:00Z'),
        confirmedDate: new Date('2025-01-02T13:30:00Z')
    }
];

// Insert the sample orders
db.orders.insertMany(sampleOrders);

// Create indexes for better query performance
db.orders.createIndex({ userId: 1 });
db.orders.createIndex({ status: 1 });
db.orders.createIndex({ orderDate: -1 });

// Verify data insertion
const orderCount = db.orders.countDocuments();
print(`Order database initialization completed successfully`);
print(`Total orders created: ${orderCount}`);

// Display summary by status
const statusSummary = db.orders.aggregate([
    { $group: { _id: '$status', count: { $sum: 1 } } },
    { $sort: { _id: 1 } }
]).toArray();

print('\nOrders by status:');
statusSummary.forEach(item => {
    print(`  ${item._id}: ${item.count}`);
});
