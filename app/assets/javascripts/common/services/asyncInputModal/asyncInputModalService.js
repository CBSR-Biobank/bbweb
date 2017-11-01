/**
 *
 */

/*
 * This service provides a single function which opens a modal.
 */
/* @ngInject */
class AsyncInputModalService {

  constructor($uibModal) {
    Object.assign(this, { $uibModal })
  }

  /**
   * A modal that allows the user to select a value returned by the server.
   *
   * @param {string} heading the heading to display as the modal's title.
   *
   * @param {string} label the label to display next to the input field.
   *
   * @param {string} placeholder a message to display in the input field when no value is present.
   *
   * @param {string} noResultsMessage the message to display to the user if the input they provided does not
   *                 yield any results.
   *
   * @param {function} getResults the function that is called to get the values from the server.
   *
   * @return {uibModalInstance} The instance of the modal that was opened. This is a ui-bootstrap class.
   */
  open(heading,
       label,
       placeholder,
       noResultsMessage,
       getResults) {

    class ModalController {

      constructor() {
        Object.assign(this, {
          heading,
          label,
          placeholder,
          noResultsMessage
        })

        this.value = undefined;
      }

       okPressed() {
        modal.close(this.value);
      }

       closePressed() {
        modal.dismiss('cancel');
      }

       getValues(viewValue) {
        return getResults(viewValue);
      }

       valueSelected(item) {
        this.value = item;
      }
    }

    var modal = this.$uibModal.open({
      template: require('./asyncInputModal.html'),
      controller: ModalController,
      controllerAs: 'vm',
      backdrop: true,
      keyboard: false,
      modalFade: true
    });

    return modal;

  }
}

export default ngModule => ngModule.service('asyncInputModal', AsyncInputModalService)
