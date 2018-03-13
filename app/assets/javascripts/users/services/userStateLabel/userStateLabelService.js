/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { LabelService } from '../../../base/services/LabelService';

/**
 * An AngularJS service that converts a {@link domain.users.userStates.UserState UserState} to a *translated
 * string* that can be displayed to the user.
 *
 * @memberOf users.services
 */
class userStateLabelService extends LabelService {

  /**
   * @param {base.BbwebError} BbwebError - AngularJS factory for exceptions.
   *
   * @param {object} UserState - AngularJS constant that enumerates all the user states.
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

  stateToLabelFunc(state) {
    return this.getLabel(state);
  }

}

export default ngModule => ngModule.service('userStateLabelService', userStateLabelService)
