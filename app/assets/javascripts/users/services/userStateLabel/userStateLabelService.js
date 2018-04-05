/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { LabelService } from '../../../base/services/LabelService';

/**
 * An AngularJS service that converts a {@link domain.users.UserState.UserState UserState} to a *translated
 * string* that can be displayed to the user.
 *
 * @memberOf users.services
 */
class userStateLabelService extends LabelService {

  /**
   * @param {base.BbwebError} BbwebError
   *
   * @param {domain.users.UserState.UserState} UserState
   *
   * @param {object} gettextCatalog - The service that allows strings to be translated to other languages.
   */
  constructor(BbwebError, UserState, gettextCatalog) {
    'ngInject';

    super(BbwebError, [
      { id: UserState.REGISTERED, label: () => gettextCatalog.getString('Registered') },
      { id: UserState.ACTIVE,     label: () => gettextCatalog.getString('Active') },
      { id: UserState.LOCKED,     label: () => gettextCatalog.getString('Locked') },
    ]);
    Object.assign(this, { UserState, gettextCatalog });
  }

  /**
   * Returns the function that should be called to display the label for a {@link
   * domain.users.UserState.UserState UserState}.
   *
   * @param {domain.users.UserState.UserState} state - the state to get a function for.
   *
   * @return {function} a function that returns a label that can be displayed to the user.
   */
  stateToLabelFunc(state) {
    return this.getLabel(state);
  }

}

export default ngModule => ngModule.service('userStateLabelService', userStateLabelService)
