initialize xacmlExecutor
policy location:
/home/qiwei/xacmlProject/pdpbtester/pdpbTester/resources
craete the pdp
craete the pdp
xacmlExecutor initiazlize property successfully
created xacml executor
[23:03.024] [b.r.ServerViewController.<init>(72)] [INFO ] - Using view stored on disk
[23:04.016] [b.c.c.n.NettyClientServerCommunicationSystemServerSide.<init>(152)] [INFO ] - ID = 0
[23:04.016] [b.c.c.n.NettyClientServerCommunicationSystemServerSide.<init>(153)] [INFO ] - N = 4
[23:04.016] [b.c.c.n.NettyClientServerCommunicationSystemServerSide.<init>(154)] [INFO ] - F = 1
[23:04.016] [b.c.c.n.NettyClientServerCommunicationSystemServerSide.<init>(155)] [INFO ] - Port (client <-> server) = 11000
[23:04.016] [b.c.c.n.NettyClientServerCommunicationSystemServerSide.<init>(157)] [INFO ] - Port (server <-> server) = 11001
[23:04.016] [b.c.c.n.NettyClientServerCommunicationSystemServerSide.<init>(159)] [INFO ] - requestTimeout = 5000
[23:04.017] [b.c.c.n.NettyClientServerCommunicationSystemServerSide.<init>(160)] [INFO ] - maxBatch = 1024
[23:04.017] [b.c.c.n.NettyClientServerCommunicationSystemServerSide.<init>(163)] [INFO ] - Binded replica to IP address 127.0.0.1
[23:04.017] [b.c.c.n.NettyClientServerCommunicationSystemServerSide.<init>(168)] [INFO ] - SSL/TLS enabled, protocol version: TLSv1.2
[23:04.019] [b.tom.ServiceReplica.init(160)] [INFO ] - In current view: ID:0; F:1; Processes:0(/127.0.0.1:11000),1(/127.0.0.1:11010),2(/127.0.0.1:11020),3(/127.0.0.1:11030),
[23:04.053] [b.s.s.StandardStateManager.init(339)] [INFO ] - statemanager is initialized
[23:04.054] [b.s.s.StandardStateManager.init(63)] [INFO ] - standardstatemanager is initialized
[23:04.054] [b.t.c.DeliveryThread.run(241)] [INFO ] - Retrieving State
[23:09.182] [b.s.s.StandardStateManager.currentConsensusIdReceived(316)] [INFO ] - Replica state is up to date
[23:09.182] [b.t.c.DeliveryThread.run(246)] [INFO ] - 
		###################################
		    Ready to process operations    
		###################################
[23:11.445] [b.c.c.n.NettyClientServerCommunicationSystemServerSide.channelActive(247)] [INFO ] - Session Created, active clients=0
[23:11.532] [b.tom.core.TOMLayer.createPropose(447)] [INFO ] - add all 0 unresponded batches at checkpoint block, but some of them might be removed later, checkpoint period is 10000
[23:16.464] [b.c.c.n.NettyClientServerCommunicationSystemServerSide.channelActive(247)] [INFO ] - Session Created, active clients=1
[23:16.518] [b.c.c.n.NettyClientServerCommunicationSystemServerSide.channelActive(247)] [INFO ] - Session Created, active clients=1
[23:16.550] [b.c.c.n.NettyClientServerCommunicationSystemServerSide.channelActive(247)] [INFO ] - Session Created, active clients=1
[23:16.586] [b.c.c.n.NettyClientServerCommunicationSystemServerSide.channelActive(247)] [INFO ] - Session Created, active clients=1
[23:16.627] [b.c.c.n.NettyClientServerCommunicationSystemServerSide.channelActive(247)] [INFO ] - Session Created, active clients=1
[23:16.643] [b.c.c.n.NettyClientServerCommunicationSystemServerSide.channelActive(247)] [INFO ] - Session Created, active clients=1
[23:16.663] [b.c.c.n.NettyClientServerCommunicationSystemServerSide.channelActive(247)] [INFO ] - Session Created, active clients=1
[23:16.694] [b.c.c.n.NettyClientServerCommunicationSystemServerSide.channelActive(247)] [INFO ] - Session Created, active clients=1
[23:16.711] [b.c.c.n.NettyClientServerCommunicationSystemServerSide.channelActive(247)] [INFO ] - Session Created, active clients=1
[23:16.746] [b.c.c.n.NettyClientServerCommunicationSystemServerSide.channelActive(247)] [INFO ] - Session Created, active clients=1
need update PDP from 0 to 315, time costs 0 ms
[23:26.389] [b.t.s.PDPB.POrder.ThroughputMeasure(578)] [INFO ] - Throughput = 39.246468 operations/sec (Maximum observed: 39.246468 ops/sec)
[23:36.891] [b.tom.core.TOMLayer.createPropose(456)] [INFO ] - rex batch: (316, 0), (324, 0), 
need update PDP from 324 to 1771, time costs 0 ms
[23:39.706] [b.t.s.PDPB.POrder.ThroughputMeasure(578)] [INFO ] - Throughput = 75.09199 operations/sec (Maximum observed: 75.09199 ops/sec)
[23:46.924] [b.tom.core.TOMLayer.createPropose(456)] [INFO ] - rex batch: (1771, 0), 
need update PDP from 1773 to 2521, time costs 0 ms
[23:52.924] [b.t.s.PDPB.POrder.ThroughputMeasure(578)] [INFO ] - Throughput = 75.66013 operations/sec (Maximum observed: 75.66013 ops/sec)
[23:56.927] [b.tom.core.TOMLayer.createPropose(456)] [INFO ] - rex batch: (1773, 0), 
need update PDP from 2523 to 3271, time costs 0 ms
[24:05.952] [b.t.s.PDPB.POrder.ThroughputMeasure(578)] [INFO ] - Throughput = 76.75775 operations/sec (Maximum observed: 76.75775 ops/sec)
[24:06.919] [b.tom.core.TOMLayer.createPropose(456)] [INFO ] - rex batch: (2523, 0), 
need update PDP from 3271 to 4034, time costs 0 ms
