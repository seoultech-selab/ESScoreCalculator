# ESScoreCalculator

텍스트 기반 `EditScript`를 노드 기반의 `Script`로 변경하는 도구들과 Change Marker에서 생성된 `changes.obj`를 `Benchmark`로 변경하는 도구를 제공.  


### 노드 기반 스크립트 생성

Change Marker에서 수집된 `EditScript`는 텍스트로 변경된 부분을 저장함.  
이를 파싱된 소스코드와 비교하여 AST 노드 기반의 `model.Script`의 객체로 변환하는 방법을 `ScriptConverter`가 제공함. 

### Bechmark 생성 및 점수 계산

`CreateBenchmark`는 Change Marker를 이용해 생성된 여러 개의 `changes.obj` 파일로부터 `EditScript`를 읽어들여 변환한 후, 이를 이용해 `Benchmark`를 생성함. 이렇게 생성된 `Benchmark`는 `ScoreCalculator`를 생성할 때 사용됨.   
`ScoreCalculator`는 `Script`객체 사이의 유사도를 계산하는 방법을 제공하고, SCD도구가 생성한 edit script를 `Script`로 변환하여 입력하면
해당 edit script를 `Benchmark`에 저장된 script들과 비교하여 여러 형태의 점수를 계산해주는 방법도 제공함.   

