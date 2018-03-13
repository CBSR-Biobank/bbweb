/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

let service;

/**
 * An AngularJS service that allows for the creation of a modal to let the user to select a {@link
 * domain.centres.Centre Centre} {@link domain.Location Location}.
 *
 * @memberOf centres.services
 */
class CentreLocationsModalService {

  constructor($uibModal, Centre) {
    'ngInject';
    Object.assign(this, { $uibModal, Centre });
    service = this;
  }

  /**
   * Opens a modal that lets the user select a {@link domain.centres.Centre Centre} {@link domain.Location
   * Location} by entering the partial name of a location. The partial name is then sent to the server and the
   * results displayed in a **UI Bootstrap** *UIB Typeahead* attached to the modal's text input.
   *
   * @param {string} heading - the text to display as the modals header.
   *
   * @param {string} label - The label for the input field.
   *
   * @param {string} placeholder - The place holder text to display in the input field.
   *
   * @param {string} [value] - The initial value to display in the input field.
   *
   * @param {Array<domain.centres.CentreLocationInfo>} [locationInfosToOmit=[]] - the locations to omit from
   * the results returned by the server.
   *
   * @return {object} The **UI Bootstrap Modal** instance.
   */
  open(heading, label, placeholder, value, locationInfosToOmit = []) {
    const locationIdsToOmit =  _.map(locationInfosToOmit, 'locationId');
    let modal = null;

    class ModalController {

      constructor() {
        Object.assign(this, { heading, label, placeholder });
        this.locationInfo = value;
      }


      okPressed() {
        modal.close(this.locationInfo);
      }

      closePressed() {
        modal.dismiss('cancel');
      }

      getCentreLocationInfo(filter) {
        return service.Centre.locationsSearch(filter)
          .then(locations => {
            _.remove(locations,
                     location => _.includes(locationIdsToOmit, location.locationId));
            return locations;
          });
      }
    }

    modal = service.$uibModal.open({
      template: require('./centreLocationsModal.html'),
      controller: ModalController,
      controllerAs: 'vm',
      backdrop: true,
      keyboard: true,
      modalFade: true
    });

    return modal;
  }

}

export default ngModule => ngModule.service('centreLocationsModalService', CentreLocationsModalService)
