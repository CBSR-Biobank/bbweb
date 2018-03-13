/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { LabelService } from '../../../base/services/LabelService';

/**
 * An AngularJS service that converts a {@link domain.participants.specimenStates.SpecimenState SpecimenState}
 * to a *translated string* that can be displayed to the user.
 *
 * @memberOf collection.services
 */
class SpecimenStateLabelService extends LabelService {

  /**
   * @param {base.BbwebError} BbwebError - AngularJS factory for exceptions.
   *
   * @param {domain.participants.SpecimenState} SpecimenState - AngularJS constant that enumerates all the
   * {@link domain.participant.Specimen Specimen} states.
   *
   * @param {object} gettextCatalog - The service that allows strings to be translated to other languages.
   */
  constructor(BbwebError, SpecimenState, gettextCatalog) {
    'ngInject';
    super(BbwebError, [
      { id: SpecimenState.USABLE,   label: () => gettextCatalog.getString('Usable') },
      { id: SpecimenState.UNUSABLE, label: () => gettextCatalog.getString('Unusable') }
    ]);
    Object.assign(this, { SpecimenState, gettextCatalog });
  }

  /**
   * Returns the function that should be called to display the label for a {@link
   * domain.participants.SpecimenState SpecimenState}.
   *
   * @param {domain.participants.SpecimenState} state - the state to get a function for.
   *
   * @return {function} a function that returns a label that can be displayed to the user.
   */
  stateToLabelFunc(state) {
    return this.getLabel(state);
  }

}

export default ngModule => ngModule.service('specimenStateLabelService', SpecimenStateLabelService)
