package gr.aueb.cf10.gymapp.service;

import gr.aueb.cf10.gymapp.dto.MemberInsertDTO;
import gr.aueb.cf10.gymapp.dto.MemberReadOnlyDTO;

import java.util.List;
import java.util.UUID;

public interface IMemberService {

    MemberReadOnlyDTO createMember(MemberInsertDTO insertDTO);

    MemberReadOnlyDTO updateMember(UUID uuid, MemberInsertDTO insertDTO);

    void deleteMember(UUID uuid);

    MemberReadOnlyDTO getMemberByUuid(UUID uuid);

    List<MemberReadOnlyDTO> getAllMembers();
}
