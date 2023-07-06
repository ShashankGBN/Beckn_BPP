package com.tibil.BecknBPP.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tibil.BecknBPP.model.Candidate;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

	public List<Candidate> findDistinctByDesignations_NameContainingIgnoreCaseAndSkills_NameInAndAvailability(String designation, List<String> skills, boolean availability);
}

