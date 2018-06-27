/**
 * This AngularJS Service allows the user to select a {@link domain.studies.Study Study} or all studies
 * for a {@link domain.access.Membership Membership}.
 *
 * @memberOf admin.users.services
 */

import _ from 'lodash';

class MembershipChangeStudiesModalService {

  constructor($uibModal) {
    'ngInject';
    Object.assign(this,
                  {
                    $uibModal
                  });
  }

  /**
   * Opens a modal dialog that allows the user to select a study or all studies.
   *
   * @param {domain.access.Membership} membership - the membership to add the study, or all studies, to.
   */
  open(membership) {
    /*
     * The controller used by this modal.
     */
    class ModalController {

      constructor($q, $uibModalInstance, StudyName) {
        'ngInject'
        Object.assign(this,
                      {
                        $q,
                        $uibModalInstance,
                        StudyName,
                        studyData: _.cloneDeep(membership.studyData),
                        allStudiesMembership: membership.studyData.allEntities,
                        tags: membership.studyData.entityData.map(studyInfo => ({
                            label: studyInfo.name,
                          obj: studyInfo
                        }))
                      });
      }

      /*
       * Called when the user presses the modal's OK button.
       */
      okPressed() {
        this.$uibModalInstance.close(this.studyData);
      }

      /*
       * Called when the user presses the modal's CANCEL button.
       */
      closePressed() {
        this.$uibModalInstance.dismiss('cancel');
      }

      getStudyNames(viewValue) {
        return this.StudyName.list({ filter: 'name:like:' + viewValue},
                                   membership.studyData.entityData)
          .then((nameObjs) =>
                nameObjs.map((nameObj) => ({ label: nameObj.name, obj: nameObj })));
      }

      allStudiesChanged() {
        if (this.allStudiesMembership) {
          this.studyData = {
            allEntities: true,
            entityData: []
          }
        }
      }

      studySelected(studyName) {
        this.studyData.allEntities = false;
        this.studyData.entityData.push(studyName);
      }

      removeStudy(studyName) {
        this.studyData.entityData = this.studyData.entityData
          .filter(studyInfo => studyInfo.id !== studyName.id);
      }
    }

    const modal = this.$uibModal.open({
      template: require('./membershipChangeStudiesModal.html'),
      controller: ModalController,
      controllerAs: 'vm',
      backdrop: true,
      keyboard: false,
      modalFade: true
    });

    return modal;
  }

}

export default ngModule => ngModule.service('MembershipChangeStudiesModal',
                                           MembershipChangeStudiesModalService)
