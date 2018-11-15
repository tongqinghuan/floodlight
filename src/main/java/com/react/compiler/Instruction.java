package com.react.compiler;

public class Instruction{
	Scope scope;
	Constraint constraint;
	Annotation annotation;
	int priority;
	boolean is_validation;

	public Instruction(Scope scope,Constraint constraint,Annotation annotation) {
		this.scope=scope;
		this.constraint=constraint;
		this.annotation=annotation;
		this.priority=0;
		this.is_validation=false;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Instruction other = (Instruction) obj;
		if (constraint == null) {
			if (other.constraint != null)
				return false;
		} else if (!constraint.equals(other.constraint))
			return false;
		if (priority != other.priority)
			return false;
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		return true;
	}

	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	public Constraint getConstraint() {
		return constraint;
	}

	public void setConstraint(Constraint constraint) {
		this.constraint = constraint;
	}

	public Annotation getAnnotation() {
		return annotation;
	}

	public void setAnnotation(Annotation annotation) {
		this.annotation = annotation;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public boolean isIs_validation() {
		return is_validation;
	}

	public void setIs_validation(boolean is_validation) {
		this.is_validation = is_validation;
	}

	public String toString() {
		return "Instruction [scope=" + scope + ", constraint=" + constraint + ", annotation=" + annotation
				+ ", priority=" + priority + ", is_validation=" + is_validation + "]";
	}
	
	/*public static void test() {

		HashMap<String,List<Instruction>> flow_semantic_rules=new HashMap<String,List<Instruction>>();
		List<Instruction> instruction=new ArrayList<Instruction>();
		Scope scope=new Scope("A","11.1.0.0/16");
		List<String> actions=new ArrayList<String>();
		actions.add("forward");
		List<Integer> ports=new ArrayList<Integer>();
		ports.add(3);
		Constraint constriant=new Constraint(actions,ports);
		Annotation annotation=new Annotation();
		annotation.annotation.add(new TwoTuple("towards","B"));
		annotation.annotation.add(new TwoTuple("forbid","A"));
		instruction.add(new Instruction(scope,constriant,annotation));
		System.out.println("successfully");
		
		List<Instruction> instruction1=new ArrayList<Instruction>();
		Scope scope1=new Scope("B","11.1.0.0/16");
		List<String> actions1=new ArrayList<String>();
		actions.add("forward");
		List<Integer> ports1=new ArrayList<Integer>();
		ports.add(1);
		Constraint constriant1=new Constraint(actions,ports);
		Annotation annotation1=new Annotation();
		annotation1.annotation.add(new TwoTuple("towards","G"));
		annotation1.annotation.add(new TwoTuple("forbid","B"));
		instruction.add(new Instruction(scope,constriant,annotation));
		
		List<Instruction> instruction2=new ArrayList<Instruction>();
		Scope scope2=new Scope("D","11.1.0.0/16");
		List<String> actions2=new ArrayList<String>();
		actions.add("forward");
		List<Integer> ports2=new ArrayList<Integer>();
		ports.add(3);
		Constraint constriant2=new Constraint(actions,ports);
		Annotation annotation2=new Annotation();
		annotation2.annotation.add(new TwoTuple("towards","G"));
		annotation2.annotation.add(new TwoTuple("forbid","D"));
		instruction.add(new Instruction(scope,constriant,annotation));
		
		List<Instruction> instruction3=new ArrayList<Instruction>();
		Scope scope3=new Scope("F","11.1.0.0/16");
		List<String> actions3=new ArrayList<String>();
		actions.add("forward");
		List<Integer> ports3=new ArrayList<Integer>();
		ports.add(2);
		Constraint constriant3=new Constraint(actions,ports);
		Annotation annotation3=new Annotation();
		annotation3.annotation.add(new TwoTuple("towards","G"));
		annotation3.annotation.add(new TwoTuple("forbid","F"));
		instruction.add(new Instruction(scope,constriant,annotation));
		
		List<Instruction> instruction4=new ArrayList<Instruction>();
		Scope scope4=new Scope("G","11.1.0.0/16");
		List<String> actions4=new ArrayList<String>();
		actions.add("forward");
		List<Integer> ports4=new ArrayList<Integer>();
		ports.add(3);
		Constraint constriant4=new Constraint(actions,ports);
		Annotation annotation4=new Annotation();
		annotation4.annotation.add(new TwoTuple("fixed_foward","3"));
		annotation4.annotation.add(new TwoTuple("forbid","G"));
		instruction.add(new Instruction(scope,constriant,annotation));
		
		flow_semantic_rules.put("11.1.0.0/16", instruction);
		System.out.println(flow_semantic_rules);
		
	}
	*/
}
