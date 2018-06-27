/**
 * This AngularJS Service allows the user to select a {@link domain.centres.Centre Centre} or all centres
 * for a {@link domain.access.Membership Membership}.
 *
 * @memberOf admin.users.services
 */

import _ from 'lodash';

class MembershipChangeCentresModalService {

  constructor($uibModal) {
    'ngInject';
    Object.assign(this,
                  {
                    $uibModal
                  });
  }

  /**
   * Opens a modal dialog that allows the user to select a centre or all centres.
   *
   * @param {domain.access.Membership} membership - the membership to add the centre, or all centres, to.
   */
  open(membership) {
    /*
     * The controller used by this modal.
     */
    class ModalController {

      constructor($q, $uibModalInstance, CentreName) {
        'ngInject'
        Object.assign(this,
                      {
                        $q,
                        $uibModalInstance,
                        CentreName,
                        centreData: _.cloneDeep(membership.centreData),
                        allCentresMembership: membership.centreData.allEntities,
                        tags: membership.centreData.entityData.map(centreInfo => ({
                          label: centreInfo.name,
                          obj: centreInfo
                        }))
                      });
      }

      /*
       * Called when the user presses the modal's OK button.
       */
      okPressed() {
        this.$uibModalInstance.close(this.centreData);
      }

      /*
       * Called when the user presses the modal's CANCEL button.
       */
      closePressed() {
        this.$uibModalInstance.dismiss('cancel');
      }

      getCentreNames(viewValue) {
        return this.CentreName.list({ filter: 'name:like:' + viewValue},
                                   membership.centreData.entityData)
          .then((nameObjs) =>
                nameObjs.map((nameObj) => ({ label: nameObj.name, obj: nameObj })));
      }

      allCentresChanged() {
        if (this.allCentresMembership) {
          this.centreData = {
            allEntities: true,
            entityData: []
          }
        }
      }

      centreSelected(centreName) {
        this.centreData.allEntities = false;
        this.centreData.entityData.push(centreName);
      }

      removeCentre(centreName) {
        this.centreData.entityData = this.centreData.entityData
          .filter(centreInfo => centreInfo.id !== centreName.id);
      }
    }

    const modal = this.$uibModal.open({
      template: require('./membershipChangeCentresModal.html'),
      controller: ModalController,
      controllerAs: 'vm',
      backdrop: true,
      keyboard: false,
      modalFade: true
    });

    return modal;
  }

}

export default ngModule => ngModule.service('MembershipChangeCentresModal',
                                           MembershipChangeCentresModalService)
