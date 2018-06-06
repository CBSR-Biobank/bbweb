/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

//let service;

/**
 * An AngularJS service that lets the user open a modal dialog box.
 *
 * @memberOf admin.studies.services
 */
class ProcessingTypeInputModalService {

  constructor($uibModal) {
    'ngInject';
    Object.assign(this,
                  {
                    $uibModal
                  })
    //service = this;
  }

  /**
   * Creates a modal that allows the user to change the input specimen on a {@link
   * domain.studies.ProcessingType ProcessingType}.
   *
   * @param {domain.studies.Study} study
   *
   * @param {domain.studies.ProcessingType} processingType
   *
   * @return {object} The "UI Bootstrap" modal instance.
   */
  open(study, processingType) {
    /*
     * The controller used by this modal.
     */
    class ModalController {

      constructor($q, $uibModalInstance, ProcessingTypeAdd) {
        'ngInject'
        Object.assign(this,
                      {
                        $q,
                        $uibModalInstance,
                        ProcessingTypeAdd,
                        study,
                        processingType
                      });
      }

      getCollectionSpecimenDefinitions() {
        return this.ProcessingTypeAdd.getCollectionSpecimenDefinitions(this.study);
      }

      getProcessedSpecimenDefinitions() {
        return this.ProcessingTypeAdd.getProcessedSpecimenDefinitions(this.study);
      }

      /*
       * Called when the user presses the modal's OK button.
       */
      okPressed() {
        this.$uibModalInstance.close(this.processingType.specimenProcessing.input);
      }

      /*
       * Called when the user presses the modal's CANCEL button.
       */
      closePressed() {
        this.$uibModalInstance.dismiss('cancel');
      }
    }

    const modal = this.$uibModal.open({
      template: require('./processingTypeInputModal.html'),
      controller: ModalController,
      controllerAs: 'vm',
      backdrop: true,
      keyboard: false,
      modalFade: true,
      size: 'lg'
    });

    return modal;
  }

}

export default ngModule => ngModule.service('ProcessingTypeInputModal', ProcessingTypeInputModalService)
