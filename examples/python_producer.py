from confluent_kafka import Producer


p = Producer({'bootstrap.servers': 'localhost'})

def delivery_report(err, msg):
    """ Called once for each message produced to indicate delivery result.
        Triggered by poll() or flush(). """
    if err is not None:
        print('Message delivery failed: {}'.format(err))
    else:
        print('Message delivered to {} [{}]'.format(msg.topic(), msg.partition()))

for x in range(10):
    p.produce('test.foo', 'das ist nur ein Test: %d' % x, callback=delivery_report)

p.flush()
