import smtplib

server = smtplib.SMTP('smtp.gmail.com', 587)
server.starttls()
server.login('fanam6500@@gmail.com', 'blzr bchs kajr bgzh')
server.sendmail('fanam6500@gmail.com', 'i201844@nu.edu.pk', 'This is a test email.')
server.quit()
