/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * This AngularJS Service allows the user to open a modal dialog box.
 *
 * @memberOf common.services
 */
class AsyncInputModalService {

  constructor($uibModal) {
    'ngInject'
    Object.assign(this, { $uibModal })
  }

  /**
   * Opens a modal that allows the user to enter some text and then select a matching value.
   *
   * Matches for this text are then retrieved asynchronously from the server, and the matches are displayed
   * in a Bootstrap Typeahead input (`uib-typeahead`).
   *
   * If the user presses the **OK** button, the object returned by the modal is the object corresponding
   * to the label selected by the user. See {@link common.services.AsyncInputModalService.GetResults
   * GetResults}.
   *
   * @param {string} heading the heading to display as the modal's title.
   *
   * @param {string} label the label to display next to the input field.
   *
   * @param {string} placeholder a message to display in the input field when no value is present.
   *
   * @param {string} noResultsMessage the message to display to the user if the input they provided does not
   * yield any results.
   *
   * @param {common.services.AsyncInputModalService.GetResults} getResults the function that is called to
   * get the matching values from the server.
   *
   * @return {uibModalInstance} The instance of the modal that was opened. This is a `ui-bootstrap` class.
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

/**
 * The callback function called by component {@link common.services.AsyncInputModalService
 * AsyncInputModalService} to retrieve the results matching the input entered by the user.
 *
 * @callback common.services.AsyncInputModalService.GetResults
 *
 * @param {string} viewValue - the text entered by the user.
 *
 * @return {Promise<Array<common.services.AsyncInputModalService.Results>>}
 */

/**
 * One of the objects that is returned by {@link common.services.AsyncInputModalService.GetResults
 * GetResults}.
 *
 * @typedef common.services.AsyncInputModalService.Results
 *
 * @type object
 *
 * @property {string} label - the label to display to the user.
 *
 * @property {object} obj - The object the label represents.
 */

export default ngModule => ngModule.service('asyncInputModal', AsyncInputModalService)
