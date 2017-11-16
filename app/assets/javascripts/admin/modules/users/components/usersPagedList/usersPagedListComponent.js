/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

import { PagedListController } from '../../../../../common/controllers/PagedListController';
import _ from 'lodash';

/*
 * Controller for this component.
 */
class Controller extends PagedListController {

  constructor($log,
              $state,
              User,
              UserState,
              userStateLabelService,
              UserCounts,
              gettextCatalog,
              EmailFilter,
              NameFilter,
              StateFilter,
              resourceErrorService) {
    'ngInject';

    const stateData = _.values(UserState).map((state) => ({
      id: state,
      label: userStateLabelService.stateToLabelFunc(state)
    }));

    super($log,
          $state,
          gettextCatalog,
          {
            nameFilter:  new NameFilter(),
            emailFilter: new EmailFilter(),
            stateFilter: new StateFilter(true, stateData, 'all')
          },
          [
            { id: 'name',  labelFunc: () => gettextCatalog.getString('Name') },
            { id: 'email', labelFunc: () => gettextCatalog.getString('Email') },
            { id: 'state', labelFunc: () => gettextCatalog.getString('State')  }
          ],
          5);

    Object.assign(this,
                  {
                    User,
                    UserState,
                    userStateLabelService,
                    UserCounts,
                    EmailFilter,
                    NameFilter,
                    StateFilter,
                    resourceErrorService
                  });

    this.stateLabelFuncs = {};
    _.values(UserState).forEach((state) => {
      this.stateLabelFuncs[state] = userStateLabelService.stateToLabelFunc(state);
    });
  }

  $onInit() {
    this.emailFilter = '';
    this.counts      = {};

    this.UserCounts.get()
      .then((counts) => {
        this.userCounts = counts;
        this.haveUsers  = (this.userCounts.total > 0);
        super.$onInit();
      })
      .catch(this.resourceErrorService.checkUnauthorized());
  }

  getItems(options) {
    return this.UserCounts.get()
      .then((counts) => {
        this.counts = counts;
        return this.User.list(options);
      });
  }

  getItemIcon(user) {
    if (user.isRegistered()) {
      return 'glyphicon-cog';
    }
    if (user.isActive()) {
      return 'glyphicon-user';
    }
    if (user.isLocked()) {
      return 'glyphicon-lock';
    }
    throw new Error('invalid user state: ' + user.state);
  }
}

/**
 * Displays studies in a panel list.
 *
 * @return {object} An AngularJS component.
 */
const component = {
  template: require('./usersPagedList.html'),
  controller: Controller,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('usersPagedList', component)
