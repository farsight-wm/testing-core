package farsight.testing.core.advice;

import java.util.HashMap;
import java.util.Map;

import com.wm.data.IData;

import farsight.testing.core.advice.remit.Remit;
import farsight.testing.core.interceptor.FlowPosition;
import farsight.testing.core.interceptor.Interceptor;
import farsight.testing.core.pointcut.PointCut;

public class Advice {

	private final PointCut pointCut;
	private final Interceptor interceptor;
	private final String id;
	private AdviceState adviceState = AdviceState.NEW;
	private final Remit remit;

	public Advice(String id, Remit remit, PointCut pointCut, Interceptor interceptor) {
		this.pointCut = pointCut;
		this.interceptor = interceptor;
		this.id = id;
		this.remit = remit;
	}

	public Remit getRemit() {
		return remit;
	}

	public PointCut getPointCut() {
		return pointCut;
	}

	public boolean isApplicable(FlowPosition pipelinePosition, IData idata){
		return pointCut.isApplicable(pipelinePosition, idata) && remit.isApplicable();
	}
	
	public Interceptor getInterceptor() {
		return interceptor;
	}

	public String getId() {
		return id;
	}

	public AdviceState getAdviceState() {
		return adviceState;
	}

	public void setAdviceState(AdviceState adviceState) {
		this.adviceState = adviceState;
	}

	@Override
	public String toString() {
		return id + ' ' + adviceState + ' ' + pointCut + ' ' + interceptor + ' ' + pointCut.getInterceptPoint() + ' ' + remit;
	}
	
	public Map<String, Object> toMap() {
		Map<String, Object> am = new HashMap<>();
		am.put("state", adviceState.toString());
		am.put("adviceId", id);
		am.put("pointcut", pointCut.toMap());
		am.put("interceptor", interceptor.toMap());
		am.put("remit", remit.toString());
		return am;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Advice other = (Advice) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
