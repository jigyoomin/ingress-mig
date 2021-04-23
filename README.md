# ingress-mig

## Info

IBM ingress annotation 을 Nginx ingress annotation 으로 자동 변환하는 도구

## Usage
```
java -jar ingress-mig.jar {subcommand} {options} {kubeconfigs}
```
### example
```
alias im='java -jar ingress-mig.jar'

im export -t change -t canyou -f excel.xls kubeconfigs.yaml
im print -t canyou kubeconfigs.yaml
im apply 
```

## Global options

* **-t** (type) : 어노테이션 타입. 입력 안하거나 복수로 사용 가능  (-t CHANGE -t IGNORE)

    - **CHANGE** : 자동 변환 가능

    - **IGNORE** : 아무 영향 없으므로 무시

    - **CANYOU** : 분석 필요

    - **DELETE** : 삭제 해야 하는 것들

* **-d** (delete) : change 한 후 원본 어노테이션 삭제 여부 (default : false)

* **-h** (help) : Print usage
  
## Sub Command

* **apply** - 마이그레이션을 수행한다. (실제 Ingress 에 annotation 추가)
 
* **print** - annotation 종류별로 출력
    - option
      * **-i** : Ingress yaml before and after 출력

* **export** - export as excel
    - option
        * **-f** : 출력 할 excel 파일 명 

  
## kubeconfigs

작업을 수행할 클러스터의 kubeconfig 파일과 context 를 지정한 yaml 파일

지정하지 않을 경우 동일한 위치의 kubeconfigs.yaml 파일 사용

### format
```
kubeconfigs:
- configpath: C:\Users\Administrator\.kube\kube-config-dep
  context: 
  - name: cloudzcp-dep-dev
  - name: cloudzcp-dep-prod
- configpath: C:\Users\Administrator\.kube\coinnet\kube-config
```

## Build

```
gradlew clean jar
```
