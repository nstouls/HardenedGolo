module spec.Div

function div = |a, b| spec/
                        requires {
                          exists a:int.(a > 5)
                        }
                        ensures {
                          a = 5 \/ b /\ c % (a /\ 3)
                        }
                     /spec {
  return (a / b)
}

function test = {
	var myDiv = div(1, 2)
	myDiv = div(40, 1)
	return(myDiv)
}

function main = |args| {
	test()
}
