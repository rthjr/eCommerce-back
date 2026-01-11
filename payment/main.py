from bakong_khqr import KHQR

khqr = KHQR()

# Create a new KHQR

qr_string = khqr.create_qr(
    bank_account="sothen_ban@wing",
    merchant_name="Sothen Ban",
    merchant_city="Phnom Penh",
    amount=1000.00,
    currency="KHR", # KHR or USD
    store_label="Sothen SHOP",
    phone_number="85512345678",
    bill_number="TRX123456",    
    terminal_label="WebQR",
    static=False 
)

print(qr_string)
