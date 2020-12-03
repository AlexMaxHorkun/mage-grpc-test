package main

import (
	"fmt"
	"time"
	"math"
)

const (
	MyConst = 22
)

type MyStruct struct {
	Val1 int
	Val2 string
	Val3 [10]int
}

func mult(a, b int) int {
	c := MyConst
	if b < 5 {
		c = 1
	}

	return a * b * c
}

func sum(end int) int {
	var res int
	for i := 1; i <= end; i++ {
		res += i
	}

	return res
}

func doSlice(in []int) ([]int, []int) {
	mys := in[2:10]
	return mys[:2], mys[5:]
}

func printValues(arr []int) {
	for i, v := range arr {
		fmt.Printf("[%d] = %d\n", i, v)
	}
}

func doMaps() {
	mymap := make(map[string]MyStruct)
	mymap["firstkey"] = MyStruct{
		Val1: 1,
		Val2: "str1",
		Val3: [10]int{1,2},
	}
	mymap["secondKey"] = MyStruct{
		Val1: 2,
		Val2: "str2",
		Val3: [10]int{4,5},
	}
	mymap["deletedKey"] = MyStruct{
		Val1: 0,
		Val2: "",
		Val3: [10]int{},
	}
	delete(mymap, "deletedKey")
	fmt.Println(mymap)
	_, present := mymap["deleteKey"]
	fmt.Println("Deleted key present? - ", present)
}

func doCallback(strin string, fn func (str string)) {
	fn(strin)
}

func (s MyStruct) MyMethod() {
	fmt.Println(s.Val2)
}

type SubsError struct {
	Value int
	Subs int
}

func (err *SubsError) Error() string {
	return fmt.Sprintf("Tried to substruct %d from %d", err.Subs, err.Value)
}

func (s *MyStruct) MyModifyingMethod(subs int) (int, error) {
	if subs > s.Val1 {
		return s.Val1, &SubsError{
			Value: s.Val1,
			Subs:  subs,
		}
	}

	s.Val1 -= subs
	return s.Val1, nil
}

type Printable interface {
	Print()
}

func (s *MyStruct) Print() {
	fmt.Println(s)
}

func (s *MyStruct) String() string {
	return fmt.Sprint("toStringed: ", s.Val1, s.Val2, s.Val3)
}

func acceptAnything (anyval interface{}) {
	fmt.Println("I accept anything, even - ", anyval)
}

func asyncPrinterRoutine(index int, c chan int) {
	var waitTime time.Duration
	waitTime = 50
	if math.Mod(float64(index), 2) == 1 {
		waitTime *= 2
	}
	time.Sleep(waitTime * time.Millisecond)
	fmt.Println("Async printer go brrrr#", index)
	c <- index
}

func asyncPrinter(max int, exit chan int) {
	rs := make(chan int)
	for i := 1; i <= max; i++ {
		go asyncPrinterRoutine(i, rs)
	}
	finishedIndexes := 0
	for {
		<-rs
		finishedIndexes++
		if finishedIndexes == max {
			break
		}
	}
	exit <- 0
}

func testPtrs(obj *MyStruct) {
	//Modifies the object
	obj.MyModifyingMethod(1)
}

func testVal(obj MyStruct) {
	//Modifies the object
	obj.MyModifyingMethod(1)
}

func main() {
	a := 5
	b := 3
	c := MyStruct{10, "Hello there!", [10]int{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}}
	defer fmt.Println(sum(10))
	defer fmt.Println(c)
	defer fmt.Println(doSlice(c.Val3[:]))
	var testarr []int = make([]int, 5)
	testarr[0] = 3
	testarr[1] = 2
	testarr[2] = 12
	testarr[3] = 11
	testarr[4] = 22
	defer printValues(testarr)
	defer doMaps()
	defer doCallback("Printed by callback", func (str string) { fmt.Println(str)})
	defer c.MyMethod()
	c.MyModifyingMethod(3)
	defer fmt.Println("Called a method - ", c.Val1)
	var printable Printable
	printable = &c
	printable.Print()
	defer acceptAnything(c)
	defer acceptAnything(b)
	var anything interface{}
	anything = c
	var _, isStruct = anything.(MyStruct)
	defer fmt.Println("`anything` is a struct? - ", isStruct)
	anything = b
	switch val := anything.(type) {
	case MyStruct:
		fmt.Println("Is a struct! - ", val)
	case int:
		fmt.Println("Is an int", val)
	default:
		fmt.Println("What is it?")
	}
	var _, err = c.MyModifyingMethod(100)
	if err != nil {
		fmt.Println("Got error: ", err.Error())
	}
	exitChannel := make(chan int, 1)
	go asyncPrinter(10, exitChannel)
	func () {
		for {
			select {
			case <-exitChannel:
				fmt.Println("Finished with printing!!")
				return
			default:
				fmt.Println("Waiting for printer to finish....")
				time.Sleep(20 * time.Millisecond)
			}
		}
	}()

	testStr := MyStruct{
		Val1: 12,
		Val2: "str",
		Val3: [10]int{},
	}
	testPtrs(&testStr)
	fmt.Println("TestStr's value when using pointers = ", testStr.Val1)
	testStr1 := MyStruct{
		Val1: 12,
		Val2: "str",
		Val3: [10]int{},
	}
	testVal(testStr1)
	fmt.Println("TestStr's value when using values = ", testStr1.Val1)

	fmt.Println(mult(a, b))
}
