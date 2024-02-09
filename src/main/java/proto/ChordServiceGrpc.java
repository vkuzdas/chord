package proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.15.0)",
    comments = "Source: chord.proto")
public final class ChordServiceGrpc {

  private ChordServiceGrpc() {}

  public static final String SERVICE_NAME = "ChordService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<proto.Chord.FindSuccessorRequest,
      proto.Chord.FindSuccessorResponse> getFindSuccessorMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "FindSuccessor",
      requestType = proto.Chord.FindSuccessorRequest.class,
      responseType = proto.Chord.FindSuccessorResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto.Chord.FindSuccessorRequest,
      proto.Chord.FindSuccessorResponse> getFindSuccessorMethod() {
    io.grpc.MethodDescriptor<proto.Chord.FindSuccessorRequest, proto.Chord.FindSuccessorResponse> getFindSuccessorMethod;
    if ((getFindSuccessorMethod = ChordServiceGrpc.getFindSuccessorMethod) == null) {
      synchronized (ChordServiceGrpc.class) {
        if ((getFindSuccessorMethod = ChordServiceGrpc.getFindSuccessorMethod) == null) {
          ChordServiceGrpc.getFindSuccessorMethod = getFindSuccessorMethod = 
              io.grpc.MethodDescriptor.<proto.Chord.FindSuccessorRequest, proto.Chord.FindSuccessorResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "ChordService", "FindSuccessor"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Chord.FindSuccessorRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Chord.FindSuccessorResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new ChordServiceMethodDescriptorSupplier("FindSuccessor"))
                  .build();
          }
        }
     }
     return getFindSuccessorMethod;
  }

  private static volatile io.grpc.MethodDescriptor<proto.Chord.GetSuccessorRequest,
      proto.Chord.GetSuccessorResponse> getGetSuccessorMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetSuccessor",
      requestType = proto.Chord.GetSuccessorRequest.class,
      responseType = proto.Chord.GetSuccessorResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto.Chord.GetSuccessorRequest,
      proto.Chord.GetSuccessorResponse> getGetSuccessorMethod() {
    io.grpc.MethodDescriptor<proto.Chord.GetSuccessorRequest, proto.Chord.GetSuccessorResponse> getGetSuccessorMethod;
    if ((getGetSuccessorMethod = ChordServiceGrpc.getGetSuccessorMethod) == null) {
      synchronized (ChordServiceGrpc.class) {
        if ((getGetSuccessorMethod = ChordServiceGrpc.getGetSuccessorMethod) == null) {
          ChordServiceGrpc.getGetSuccessorMethod = getGetSuccessorMethod = 
              io.grpc.MethodDescriptor.<proto.Chord.GetSuccessorRequest, proto.Chord.GetSuccessorResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "ChordService", "GetSuccessor"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Chord.GetSuccessorRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Chord.GetSuccessorResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new ChordServiceMethodDescriptorSupplier("GetSuccessor"))
                  .build();
          }
        }
     }
     return getGetSuccessorMethod;
  }

  private static volatile io.grpc.MethodDescriptor<proto.Chord.GetPredecessorRequest,
      proto.Chord.GetPredecessorResponse> getGetPredecessorMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetPredecessor",
      requestType = proto.Chord.GetPredecessorRequest.class,
      responseType = proto.Chord.GetPredecessorResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto.Chord.GetPredecessorRequest,
      proto.Chord.GetPredecessorResponse> getGetPredecessorMethod() {
    io.grpc.MethodDescriptor<proto.Chord.GetPredecessorRequest, proto.Chord.GetPredecessorResponse> getGetPredecessorMethod;
    if ((getGetPredecessorMethod = ChordServiceGrpc.getGetPredecessorMethod) == null) {
      synchronized (ChordServiceGrpc.class) {
        if ((getGetPredecessorMethod = ChordServiceGrpc.getGetPredecessorMethod) == null) {
          ChordServiceGrpc.getGetPredecessorMethod = getGetPredecessorMethod = 
              io.grpc.MethodDescriptor.<proto.Chord.GetPredecessorRequest, proto.Chord.GetPredecessorResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "ChordService", "GetPredecessor"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Chord.GetPredecessorRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Chord.GetPredecessorResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new ChordServiceMethodDescriptorSupplier("GetPredecessor"))
                  .build();
          }
        }
     }
     return getGetPredecessorMethod;
  }

  private static volatile io.grpc.MethodDescriptor<proto.Chord.UpdateFingerTableRequest,
      proto.Chord.UpdateFingerTableResponse> getUpdateFingerTableMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdateFingerTable",
      requestType = proto.Chord.UpdateFingerTableRequest.class,
      responseType = proto.Chord.UpdateFingerTableResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto.Chord.UpdateFingerTableRequest,
      proto.Chord.UpdateFingerTableResponse> getUpdateFingerTableMethod() {
    io.grpc.MethodDescriptor<proto.Chord.UpdateFingerTableRequest, proto.Chord.UpdateFingerTableResponse> getUpdateFingerTableMethod;
    if ((getUpdateFingerTableMethod = ChordServiceGrpc.getUpdateFingerTableMethod) == null) {
      synchronized (ChordServiceGrpc.class) {
        if ((getUpdateFingerTableMethod = ChordServiceGrpc.getUpdateFingerTableMethod) == null) {
          ChordServiceGrpc.getUpdateFingerTableMethod = getUpdateFingerTableMethod = 
              io.grpc.MethodDescriptor.<proto.Chord.UpdateFingerTableRequest, proto.Chord.UpdateFingerTableResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "ChordService", "UpdateFingerTable"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Chord.UpdateFingerTableRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Chord.UpdateFingerTableResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new ChordServiceMethodDescriptorSupplier("UpdateFingerTable"))
                  .build();
          }
        }
     }
     return getUpdateFingerTableMethod;
  }

  private static volatile io.grpc.MethodDescriptor<proto.Chord.ClosestPrecedingFingerRequest,
      proto.Chord.ClosestPrecedingFingerResponse> getClosestPrecedingFingerMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ClosestPrecedingFinger",
      requestType = proto.Chord.ClosestPrecedingFingerRequest.class,
      responseType = proto.Chord.ClosestPrecedingFingerResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto.Chord.ClosestPrecedingFingerRequest,
      proto.Chord.ClosestPrecedingFingerResponse> getClosestPrecedingFingerMethod() {
    io.grpc.MethodDescriptor<proto.Chord.ClosestPrecedingFingerRequest, proto.Chord.ClosestPrecedingFingerResponse> getClosestPrecedingFingerMethod;
    if ((getClosestPrecedingFingerMethod = ChordServiceGrpc.getClosestPrecedingFingerMethod) == null) {
      synchronized (ChordServiceGrpc.class) {
        if ((getClosestPrecedingFingerMethod = ChordServiceGrpc.getClosestPrecedingFingerMethod) == null) {
          ChordServiceGrpc.getClosestPrecedingFingerMethod = getClosestPrecedingFingerMethod = 
              io.grpc.MethodDescriptor.<proto.Chord.ClosestPrecedingFingerRequest, proto.Chord.ClosestPrecedingFingerResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "ChordService", "ClosestPrecedingFinger"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Chord.ClosestPrecedingFingerRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Chord.ClosestPrecedingFingerResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new ChordServiceMethodDescriptorSupplier("ClosestPrecedingFinger"))
                  .build();
          }
        }
     }
     return getClosestPrecedingFingerMethod;
  }

  private static volatile io.grpc.MethodDescriptor<proto.Chord.JoinRequest,
      proto.Chord.JoinResponse> getJoinMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Join",
      requestType = proto.Chord.JoinRequest.class,
      responseType = proto.Chord.JoinResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto.Chord.JoinRequest,
      proto.Chord.JoinResponse> getJoinMethod() {
    io.grpc.MethodDescriptor<proto.Chord.JoinRequest, proto.Chord.JoinResponse> getJoinMethod;
    if ((getJoinMethod = ChordServiceGrpc.getJoinMethod) == null) {
      synchronized (ChordServiceGrpc.class) {
        if ((getJoinMethod = ChordServiceGrpc.getJoinMethod) == null) {
          ChordServiceGrpc.getJoinMethod = getJoinMethod = 
              io.grpc.MethodDescriptor.<proto.Chord.JoinRequest, proto.Chord.JoinResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "ChordService", "Join"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Chord.JoinRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Chord.JoinResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new ChordServiceMethodDescriptorSupplier("Join"))
                  .build();
          }
        }
     }
     return getJoinMethod;
  }

  private static volatile io.grpc.MethodDescriptor<proto.Chord.MoveKeysRequest,
      proto.Chord.MoveKeysResponse> getMoveKeysMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "MoveKeys",
      requestType = proto.Chord.MoveKeysRequest.class,
      responseType = proto.Chord.MoveKeysResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto.Chord.MoveKeysRequest,
      proto.Chord.MoveKeysResponse> getMoveKeysMethod() {
    io.grpc.MethodDescriptor<proto.Chord.MoveKeysRequest, proto.Chord.MoveKeysResponse> getMoveKeysMethod;
    if ((getMoveKeysMethod = ChordServiceGrpc.getMoveKeysMethod) == null) {
      synchronized (ChordServiceGrpc.class) {
        if ((getMoveKeysMethod = ChordServiceGrpc.getMoveKeysMethod) == null) {
          ChordServiceGrpc.getMoveKeysMethod = getMoveKeysMethod = 
              io.grpc.MethodDescriptor.<proto.Chord.MoveKeysRequest, proto.Chord.MoveKeysResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "ChordService", "MoveKeys"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Chord.MoveKeysRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Chord.MoveKeysResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new ChordServiceMethodDescriptorSupplier("MoveKeys"))
                  .build();
          }
        }
     }
     return getMoveKeysMethod;
  }

  private static volatile io.grpc.MethodDescriptor<proto.Chord.PutRequest,
      proto.Chord.PutResponse> getPutMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Put",
      requestType = proto.Chord.PutRequest.class,
      responseType = proto.Chord.PutResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto.Chord.PutRequest,
      proto.Chord.PutResponse> getPutMethod() {
    io.grpc.MethodDescriptor<proto.Chord.PutRequest, proto.Chord.PutResponse> getPutMethod;
    if ((getPutMethod = ChordServiceGrpc.getPutMethod) == null) {
      synchronized (ChordServiceGrpc.class) {
        if ((getPutMethod = ChordServiceGrpc.getPutMethod) == null) {
          ChordServiceGrpc.getPutMethod = getPutMethod = 
              io.grpc.MethodDescriptor.<proto.Chord.PutRequest, proto.Chord.PutResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "ChordService", "Put"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Chord.PutRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Chord.PutResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new ChordServiceMethodDescriptorSupplier("Put"))
                  .build();
          }
        }
     }
     return getPutMethod;
  }

  private static volatile io.grpc.MethodDescriptor<proto.Chord.GetRequest,
      proto.Chord.GetResponse> getGetMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Get",
      requestType = proto.Chord.GetRequest.class,
      responseType = proto.Chord.GetResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<proto.Chord.GetRequest,
      proto.Chord.GetResponse> getGetMethod() {
    io.grpc.MethodDescriptor<proto.Chord.GetRequest, proto.Chord.GetResponse> getGetMethod;
    if ((getGetMethod = ChordServiceGrpc.getGetMethod) == null) {
      synchronized (ChordServiceGrpc.class) {
        if ((getGetMethod = ChordServiceGrpc.getGetMethod) == null) {
          ChordServiceGrpc.getGetMethod = getGetMethod = 
              io.grpc.MethodDescriptor.<proto.Chord.GetRequest, proto.Chord.GetResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "ChordService", "Get"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Chord.GetRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  proto.Chord.GetResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new ChordServiceMethodDescriptorSupplier("Get"))
                  .build();
          }
        }
     }
     return getGetMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ChordServiceStub newStub(io.grpc.Channel channel) {
    return new ChordServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ChordServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new ChordServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ChordServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new ChordServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class ChordServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * invoke findSuccessor procedure on targetNode
     * </pre>
     */
    public void findSuccessor(proto.Chord.FindSuccessorRequest request,
        io.grpc.stub.StreamObserver<proto.Chord.FindSuccessorResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getFindSuccessorMethod(), responseObserver);
    }

    /**
     * <pre>
     * return targetNode.successor
     * </pre>
     */
    public void getSuccessor(proto.Chord.GetSuccessorRequest request,
        io.grpc.stub.StreamObserver<proto.Chord.GetSuccessorResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetSuccessorMethod(), responseObserver);
    }

    /**
     * <pre>
     * return targetNode.predecessor
     * </pre>
     */
    public void getPredecessor(proto.Chord.GetPredecessorRequest request,
        io.grpc.stub.StreamObserver<proto.Chord.GetPredecessorResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetPredecessorMethod(), responseObserver);
    }

    /**
     */
    public void updateFingerTable(proto.Chord.UpdateFingerTableRequest request,
        io.grpc.stub.StreamObserver<proto.Chord.UpdateFingerTableResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getUpdateFingerTableMethod(), responseObserver);
    }

    /**
     */
    public void closestPrecedingFinger(proto.Chord.ClosestPrecedingFingerRequest request,
        io.grpc.stub.StreamObserver<proto.Chord.ClosestPrecedingFingerResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getClosestPrecedingFingerMethod(), responseObserver);
    }

    /**
     */
    public void join(proto.Chord.JoinRequest request,
        io.grpc.stub.StreamObserver<proto.Chord.JoinResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getJoinMethod(), responseObserver);
    }

    /**
     */
    public void moveKeys(proto.Chord.MoveKeysRequest request,
        io.grpc.stub.StreamObserver<proto.Chord.MoveKeysResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getMoveKeysMethod(), responseObserver);
    }

    /**
     */
    public void put(proto.Chord.PutRequest request,
        io.grpc.stub.StreamObserver<proto.Chord.PutResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getPutMethod(), responseObserver);
    }

    /**
     */
    public void get(proto.Chord.GetRequest request,
        io.grpc.stub.StreamObserver<proto.Chord.GetResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getFindSuccessorMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                proto.Chord.FindSuccessorRequest,
                proto.Chord.FindSuccessorResponse>(
                  this, METHODID_FIND_SUCCESSOR)))
          .addMethod(
            getGetSuccessorMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                proto.Chord.GetSuccessorRequest,
                proto.Chord.GetSuccessorResponse>(
                  this, METHODID_GET_SUCCESSOR)))
          .addMethod(
            getGetPredecessorMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                proto.Chord.GetPredecessorRequest,
                proto.Chord.GetPredecessorResponse>(
                  this, METHODID_GET_PREDECESSOR)))
          .addMethod(
            getUpdateFingerTableMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                proto.Chord.UpdateFingerTableRequest,
                proto.Chord.UpdateFingerTableResponse>(
                  this, METHODID_UPDATE_FINGER_TABLE)))
          .addMethod(
            getClosestPrecedingFingerMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                proto.Chord.ClosestPrecedingFingerRequest,
                proto.Chord.ClosestPrecedingFingerResponse>(
                  this, METHODID_CLOSEST_PRECEDING_FINGER)))
          .addMethod(
            getJoinMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                proto.Chord.JoinRequest,
                proto.Chord.JoinResponse>(
                  this, METHODID_JOIN)))
          .addMethod(
            getMoveKeysMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                proto.Chord.MoveKeysRequest,
                proto.Chord.MoveKeysResponse>(
                  this, METHODID_MOVE_KEYS)))
          .addMethod(
            getPutMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                proto.Chord.PutRequest,
                proto.Chord.PutResponse>(
                  this, METHODID_PUT)))
          .addMethod(
            getGetMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                proto.Chord.GetRequest,
                proto.Chord.GetResponse>(
                  this, METHODID_GET)))
          .build();
    }
  }

  /**
   */
  public static final class ChordServiceStub extends io.grpc.stub.AbstractStub<ChordServiceStub> {
    private ChordServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ChordServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ChordServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ChordServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * invoke findSuccessor procedure on targetNode
     * </pre>
     */
    public void findSuccessor(proto.Chord.FindSuccessorRequest request,
        io.grpc.stub.StreamObserver<proto.Chord.FindSuccessorResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getFindSuccessorMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * return targetNode.successor
     * </pre>
     */
    public void getSuccessor(proto.Chord.GetSuccessorRequest request,
        io.grpc.stub.StreamObserver<proto.Chord.GetSuccessorResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetSuccessorMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * return targetNode.predecessor
     * </pre>
     */
    public void getPredecessor(proto.Chord.GetPredecessorRequest request,
        io.grpc.stub.StreamObserver<proto.Chord.GetPredecessorResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetPredecessorMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void updateFingerTable(proto.Chord.UpdateFingerTableRequest request,
        io.grpc.stub.StreamObserver<proto.Chord.UpdateFingerTableResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getUpdateFingerTableMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void closestPrecedingFinger(proto.Chord.ClosestPrecedingFingerRequest request,
        io.grpc.stub.StreamObserver<proto.Chord.ClosestPrecedingFingerResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getClosestPrecedingFingerMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void join(proto.Chord.JoinRequest request,
        io.grpc.stub.StreamObserver<proto.Chord.JoinResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getJoinMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void moveKeys(proto.Chord.MoveKeysRequest request,
        io.grpc.stub.StreamObserver<proto.Chord.MoveKeysResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getMoveKeysMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void put(proto.Chord.PutRequest request,
        io.grpc.stub.StreamObserver<proto.Chord.PutResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPutMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void get(proto.Chord.GetRequest request,
        io.grpc.stub.StreamObserver<proto.Chord.GetResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class ChordServiceBlockingStub extends io.grpc.stub.AbstractStub<ChordServiceBlockingStub> {
    private ChordServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ChordServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ChordServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ChordServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * invoke findSuccessor procedure on targetNode
     * </pre>
     */
    public proto.Chord.FindSuccessorResponse findSuccessor(proto.Chord.FindSuccessorRequest request) {
      return blockingUnaryCall(
          getChannel(), getFindSuccessorMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * return targetNode.successor
     * </pre>
     */
    public proto.Chord.GetSuccessorResponse getSuccessor(proto.Chord.GetSuccessorRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetSuccessorMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * return targetNode.predecessor
     * </pre>
     */
    public proto.Chord.GetPredecessorResponse getPredecessor(proto.Chord.GetPredecessorRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetPredecessorMethod(), getCallOptions(), request);
    }

    /**
     */
    public proto.Chord.UpdateFingerTableResponse updateFingerTable(proto.Chord.UpdateFingerTableRequest request) {
      return blockingUnaryCall(
          getChannel(), getUpdateFingerTableMethod(), getCallOptions(), request);
    }

    /**
     */
    public proto.Chord.ClosestPrecedingFingerResponse closestPrecedingFinger(proto.Chord.ClosestPrecedingFingerRequest request) {
      return blockingUnaryCall(
          getChannel(), getClosestPrecedingFingerMethod(), getCallOptions(), request);
    }

    /**
     */
    public proto.Chord.JoinResponse join(proto.Chord.JoinRequest request) {
      return blockingUnaryCall(
          getChannel(), getJoinMethod(), getCallOptions(), request);
    }

    /**
     */
    public proto.Chord.MoveKeysResponse moveKeys(proto.Chord.MoveKeysRequest request) {
      return blockingUnaryCall(
          getChannel(), getMoveKeysMethod(), getCallOptions(), request);
    }

    /**
     */
    public proto.Chord.PutResponse put(proto.Chord.PutRequest request) {
      return blockingUnaryCall(
          getChannel(), getPutMethod(), getCallOptions(), request);
    }

    /**
     */
    public proto.Chord.GetResponse get(proto.Chord.GetRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class ChordServiceFutureStub extends io.grpc.stub.AbstractStub<ChordServiceFutureStub> {
    private ChordServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ChordServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ChordServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ChordServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * invoke findSuccessor procedure on targetNode
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<proto.Chord.FindSuccessorResponse> findSuccessor(
        proto.Chord.FindSuccessorRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getFindSuccessorMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * return targetNode.successor
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<proto.Chord.GetSuccessorResponse> getSuccessor(
        proto.Chord.GetSuccessorRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetSuccessorMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * return targetNode.predecessor
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<proto.Chord.GetPredecessorResponse> getPredecessor(
        proto.Chord.GetPredecessorRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetPredecessorMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<proto.Chord.UpdateFingerTableResponse> updateFingerTable(
        proto.Chord.UpdateFingerTableRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getUpdateFingerTableMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<proto.Chord.ClosestPrecedingFingerResponse> closestPrecedingFinger(
        proto.Chord.ClosestPrecedingFingerRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getClosestPrecedingFingerMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<proto.Chord.JoinResponse> join(
        proto.Chord.JoinRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getJoinMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<proto.Chord.MoveKeysResponse> moveKeys(
        proto.Chord.MoveKeysRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getMoveKeysMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<proto.Chord.PutResponse> put(
        proto.Chord.PutRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getPutMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<proto.Chord.GetResponse> get(
        proto.Chord.GetRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_FIND_SUCCESSOR = 0;
  private static final int METHODID_GET_SUCCESSOR = 1;
  private static final int METHODID_GET_PREDECESSOR = 2;
  private static final int METHODID_UPDATE_FINGER_TABLE = 3;
  private static final int METHODID_CLOSEST_PRECEDING_FINGER = 4;
  private static final int METHODID_JOIN = 5;
  private static final int METHODID_MOVE_KEYS = 6;
  private static final int METHODID_PUT = 7;
  private static final int METHODID_GET = 8;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ChordServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ChordServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_FIND_SUCCESSOR:
          serviceImpl.findSuccessor((proto.Chord.FindSuccessorRequest) request,
              (io.grpc.stub.StreamObserver<proto.Chord.FindSuccessorResponse>) responseObserver);
          break;
        case METHODID_GET_SUCCESSOR:
          serviceImpl.getSuccessor((proto.Chord.GetSuccessorRequest) request,
              (io.grpc.stub.StreamObserver<proto.Chord.GetSuccessorResponse>) responseObserver);
          break;
        case METHODID_GET_PREDECESSOR:
          serviceImpl.getPredecessor((proto.Chord.GetPredecessorRequest) request,
              (io.grpc.stub.StreamObserver<proto.Chord.GetPredecessorResponse>) responseObserver);
          break;
        case METHODID_UPDATE_FINGER_TABLE:
          serviceImpl.updateFingerTable((proto.Chord.UpdateFingerTableRequest) request,
              (io.grpc.stub.StreamObserver<proto.Chord.UpdateFingerTableResponse>) responseObserver);
          break;
        case METHODID_CLOSEST_PRECEDING_FINGER:
          serviceImpl.closestPrecedingFinger((proto.Chord.ClosestPrecedingFingerRequest) request,
              (io.grpc.stub.StreamObserver<proto.Chord.ClosestPrecedingFingerResponse>) responseObserver);
          break;
        case METHODID_JOIN:
          serviceImpl.join((proto.Chord.JoinRequest) request,
              (io.grpc.stub.StreamObserver<proto.Chord.JoinResponse>) responseObserver);
          break;
        case METHODID_MOVE_KEYS:
          serviceImpl.moveKeys((proto.Chord.MoveKeysRequest) request,
              (io.grpc.stub.StreamObserver<proto.Chord.MoveKeysResponse>) responseObserver);
          break;
        case METHODID_PUT:
          serviceImpl.put((proto.Chord.PutRequest) request,
              (io.grpc.stub.StreamObserver<proto.Chord.PutResponse>) responseObserver);
          break;
        case METHODID_GET:
          serviceImpl.get((proto.Chord.GetRequest) request,
              (io.grpc.stub.StreamObserver<proto.Chord.GetResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class ChordServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ChordServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return proto.Chord.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ChordService");
    }
  }

  private static final class ChordServiceFileDescriptorSupplier
      extends ChordServiceBaseDescriptorSupplier {
    ChordServiceFileDescriptorSupplier() {}
  }

  private static final class ChordServiceMethodDescriptorSupplier
      extends ChordServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ChordServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ChordServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ChordServiceFileDescriptorSupplier())
              .addMethod(getFindSuccessorMethod())
              .addMethod(getGetSuccessorMethod())
              .addMethod(getGetPredecessorMethod())
              .addMethod(getUpdateFingerTableMethod())
              .addMethod(getClosestPrecedingFingerMethod())
              .addMethod(getJoinMethod())
              .addMethod(getMoveKeysMethod())
              .addMethod(getPutMethod())
              .addMethod(getGetMethod())
              .build();
        }
      }
    }
    return result;
  }
}
