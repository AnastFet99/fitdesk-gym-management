package gr.aueb.cf10.gymapp.service;

import gr.aueb.cf10.gymapp.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf10.gymapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf10.gymapp.core.mapper.Mapper;
import gr.aueb.cf10.gymapp.dto.MemberInsertDTO;
import gr.aueb.cf10.gymapp.dto.MemberReadOnlyDTO;
import gr.aueb.cf10.gymapp.model.Member;
import gr.aueb.cf10.gymapp.model.User;
import gr.aueb.cf10.gymapp.repository.MemberRepository;
import gr.aueb.cf10.gymapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements IMemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final Mapper mapper;

    @Override
    @Transactional
    public MemberReadOnlyDTO createMember(MemberInsertDTO insertDTO) {
        log.info("Creating member for user uuid: {}", insertDTO.userUuid());

        User user = userRepository.findByUuid(insertDTO.userUuid())
                .orElseThrow(() -> {
                    log.error("User with uuid {} not found", insertDTO.userUuid());
                    return new EntityNotFoundException("User", insertDTO.userUuid());
                });

        if (memberRepository.existsByUserId(user.getId())) {
            log.error("Member already exists for user uuid {}", insertDTO.userUuid());
            throw new EntityAlreadyExistsException("Member profile already exists for this user");
        }

        Member member = mapper.mapToMember(insertDTO, user);
        Member savedMember = memberRepository.save(member);

        log.info("Successfully created member with uuid: {}", savedMember.getUuid());
        return mapper.mapToReadOnlyDTO(savedMember);
    }

    @Override
    @Transactional
    public MemberReadOnlyDTO updateMember(UUID uuid, MemberInsertDTO insertDTO) {
        log.info("Updating member with uuid: {}", uuid);

        Member existingMember = memberRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("Member with uuid {} not found", uuid);
                    return new EntityNotFoundException("Member", uuid);
                });

        User user = userRepository.findByUuid(insertDTO.userUuid())
                .orElseThrow(() -> {
                    log.error("User with uuid {} not found", insertDTO.userUuid());
                    return new EntityNotFoundException("User", insertDTO.userUuid());
                });

        existingMember.setUser(user);
        existingMember.setPhone(insertDTO.phone());

        Member updatedMember = memberRepository.save(existingMember);
        log.info("Successfully updated member with uuid: {}", uuid);

        return mapper.mapToReadOnlyDTO(updatedMember);
    }

    @Override
    @Transactional
    public void deleteMember(UUID uuid) {
        log.info("Deleting member with uuid: {}", uuid);

        Member member = memberRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("Member with uuid {} not found", uuid);
                    return new EntityNotFoundException("Member", uuid);
                });

        memberRepository.delete(member);
        log.info("Successfully deleted member with uuid: {}", uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberReadOnlyDTO getMemberByUuid(UUID uuid) {
        log.info("Fetching member with uuid: {}", uuid);

        Member member = memberRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("Member with uuid {} not found", uuid);
                    return new EntityNotFoundException("Member", uuid);
                });

        return mapper.mapToReadOnlyDTO(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberReadOnlyDTO> getAllMembers() {
        log.info("Fetching all members");

        return memberRepository.findAll()
                .stream()
                .map(mapper::mapToReadOnlyDTO)
                .toList();
    }
}
