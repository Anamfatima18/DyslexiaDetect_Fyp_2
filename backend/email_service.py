from flask_mail import Mail, Message

mail = Mail()  # Create Mail instance

def init_email_service(app):
    mail.init_app(app)

def send_otp_email(app, recipient, otp):
    with app.app_context():
        msg = Message('Your OTP', sender=app.config['MAIL_USERNAME'], recipients=[recipient])
        msg.body = f'Your OTP is {otp}'
        mail.send(msg)
