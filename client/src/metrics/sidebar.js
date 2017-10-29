import React from 'react';
import {MenuItem, Paper} from 'material-ui';
import LineIcon from './line-chart.svg';
import BarChart from './bar-chart-7.svg';

const styles = {
  sidebar: {},
  sideBarIcon: {
    width: '24px',
    height: '24px'
  },
  sideBarItem: {
    fontSize: '0.65em'
  }
};

const sidebar = (props) =>
  <Paper zDepth={2} style={styles.sidebar}>
    <MenuItem onTouchTap={() => props.onNavigationClicked(0)}
              style={styles.sideBarItem}
              leftIcon={<img src={LineIcon} alt='Maxim Basinski, http://www.flaticon.com/authors/maxim-basinski'/>}>
      Average kb/s
    </MenuItem>
    <MenuItem onTouchTap={() => props.onNavigationClicked(1)}
              style={styles.sideBarItem}
              leftIcon={<img src={BarChart} alt='Maxim Basinski, http://www.flaticon.com/authors/maxim-basinski'/>}>
      Outgoing type chart
    </MenuItem>
    <MenuItem onTouchTap={() => props.onNavigationClicked(2)}
              style={styles.sideBarItem}
              leftIcon={<img src={BarChart} alt='Maxim Basinski, http://www.flaticon.com/authors/maxim-basinski'/>}>
      Incoming type chart
    </MenuItem>
    <MenuItem onTouchTap={() => props.onNavigationClicked(3)}
              style={styles.sideBarItem}
              leftIcon={<img src={LineIcon} alt='Maxim Basinski, http://www.flaticon.com/authors/maxim-basinski'/>}>
      Player count
    </MenuItem>
  </Paper>;

export const Sidebar = sidebar;
