import 'dart:async';

main() async {
  T1 _t1 = new T1();
  Future.delayed(Duration(seconds: 2), () => _t1.addMessage("changeConnState", 99));
  _t1.test1();
   _t1.test3();
  _t1.test2();
  Timer _timer = Timer.periodic(Duration(seconds: 1), (_timer) {
    print("alive...$_timer");
  });
}

class T1 {
  StreamController _controller = new StreamController.broadcast();

  void test1() {
    print("controller: $_controller");
  }

  void test2() {
    print("${naturalsDownFrom(3)}");
  }

  Future<void> test3() async {
    print("wait connected...");
    await state.firstWhere((_state) => 99 == _state);
    print("already connected.");
  }

  Stream<int> get state async* {
    yield* _controller.stream.where((_msg) => "changeConnState" == _msg["method"]).map((_msg) => _msg["args"]);
  }

  void addMessage(String method, Object args) {
    _controller.sink.add({"method": method, "args": args});
  }

  Iterable<int> naturalsDownFrom(int n) sync* {
    if (n > 0) {
      yield n;
      yield* naturalsDownFrom(n - 1);
    }
  }
}
