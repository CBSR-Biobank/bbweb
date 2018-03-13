/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { LabelService } from '../../../base/services/LabelService';

/**
 * An AngularJS service that converts a {@link domain.centres.CentreState CentreState} to a *translated
 * string* that can be displayed to the user.
 *
 * @memberOf centres.services
 */
class CentreStateLabelService extends LabelService {

  /**
   * @param {base.BbwebError} BbwebError - AngularJS factory for exceptions.
   *
   * @param {domain.centres.CentreState} CentreState - AngularJS constant that enumerates all the centre
   * states.
   *
   * @param {object} gettextCatalog - The service that allows strings to be translated to other languages.
   */
  constructor(BbwebError, CentreState, gettextCatalog) {
    'ngInject';
    super(BbwebError,
          [
            { id: CentreState.DISABLED, label: () => gettextCatalog.getString('Disabled') },
            { id: CentreState.ENABLED, label: () => gettextCatalog.getString('Enabled') }
          ]);
    Object.assign(this, { CentreState, gettextCatalog });
  }

  /**
   * Returns the function that should be called to display the label for a {@link domain.centres.CentreState
   * CentreState} state.
   *
   * @param {domain.centres.CentreState} state - the state to get a function for.
   *
   * @return {function} a function that returns a label that can be displayed to the user.
   */
  stateToLabelFunc(state) {
    return this.getLabel(state);
  }

}

export default ngModule => ngModule.service('centreStateLabelService', CentreStateLabelService)
